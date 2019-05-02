var WINDOW_WIDTH;
var WINDOW_HEIGHT;
var Book;
var Rendition;
var Display;
var Area;
var Body;
var nav;


var fontSize = "16px", color, background, fontFamily = "FontRoboto", bSize;
var H_cfirange, H_contents;
var N_cfirange, N_contents;
var totalChar = 150;
var offsetX, offsetY;

function PaperInit(pathE, totalCharacter, currentMarginVal){
    try{
        WINDOW_WIDTH = $(window).innerWidth();
        WINDOW_HEIGHT = $(window).innerHeight();

        Area = $("#area");
        Body = $("body");

        //Init ePub Book
        ChangeMargin(currentMarginVal);

        var path = pathE;
        Book = ePub(path);

        //Render Book
        Rendition = Book.renderTo("area",{ width: offsetX, height: offsetY});
        //Rendition = Book.renderTo("area");
        Display = Rendition.display();

        //Add Total Character
        totalChar = totalCharacter;

        //Book Ready
        BookReady();
    }
    catch(err){
        //Error Catching
        //window.JavaScriptInterface.AlertDialog("Decoding Error: 0xff001");
    }
}

function ChangeMargin(val){
    var padding = 0; //->px
    if(val == '1'){
        //Normal
        padding = 20;
    }
    else if(val == '2'){
        //Wide
        padding = 40;
    }
    else if(val == '3'){
        //Narrow
        padding = 20;
    }

    offsetX = (WINDOW_WIDTH - (2 * padding));
    offsetY = (WINDOW_HEIGHT - (2 * padding));

    Body.css({"padding": padding});
    //window.JavaScriptInterface.Log(WINDOW_WIDTH + ":" + padding + ":" + offsetX);
}

function BookReady(){
    //Book Ready Event
    Book.ready.then(function(){
        BookLoaded();
    });

}

function BookLoaded(){
    //Book.loaded.manifest.then((manifest) => { console.log(manifest) });
    //Book.loaded.spine.then((spine) => { console.log(spine) });
    //Book.loaded.metadata.then((metadata) => { console.log(metadata) });
    //Book.loaded.cover.then((cover) => { console.log(cover) });
    //Book.loaded.resources.then((resources) => { console.log(resources) });

    //Register Themes
    RegisterTheme();

    //Register Nav
    RegisterNavigation();

    //DefaultPage(); //Default Page
    GetTOC(); //Get Table Of Content

    //Reg Link Event
    RegisterRenditionLinked();

    //Register Events
    RegisterEvents();

    //Register Hooks
    RegisterHooks();

    //Register pagination
    RegisterBookPagination();

    //END
    window.JavaScriptInterface.FinishLoading(); //Set Progress Bar to Hide
}

function RegisterNavigation(){
    Book.loaded.navigation.then((navigation) => {
        nav = navigation;
        //console.log(nav);
        //get current chapter

        //var tocByHref = nav["tocByHref"];
        //console.log(tocByHref);
        //for(var key in tocByHref){
        //    console.log(key + "-->" + tocByHref[key]);
        //}
    });
}

function RegisterRenditionLinked(){
    Rendition.on("linkClicked", function(href) {
        linkClick();
    });
}

function RegisterBookPagination(){
    try{
        Book.locations.generate(totalChar).then(()=>{
            var cfi = Rendition.currentLocation().start.cfi;
            var percentage = Book.locations.percentageFromCfi(cfi);
            if(percentage == null){
                //window.JavaScriptInterface.AlertDialog("File Error 0xff005: No Pagination Percentage Create.");
                percentage = 0;
            }
            window.JavaScriptInterface.RegisterBookPaginationFinished(percentage);
        })
        .catch(err => {
            //window.JavaScriptInterface.AlertDialog("Decoding Error: 0xff002");
        });
    }
    catch(err){
        //window.JavaScriptInterface.AlertDialog("Decoding Error: 0xff003");
    }
}

function BookPagination(percentage){
    var cfi = Book.locations.cfiFromPercentage(percentage);
    Rendition.display(cfi);
}

function RegisterEvents(){
    //Page Change/Relocate Events
    PageRelocateEvent();
    //PageRelocateEvent();

    //Select Text Event
    SelectTextEvent();
}

function PageRelocateEvent(){
    Rendition.on('relocated', function(location){
        var cfi = location.start.cfi;
        var percent = Book.locations.percentageFromCfi(cfi);
        if(percent == null){
            //window.JavaScriptInterface.AlertDialog("Decoding Error: 0xff004");
            percent = 0;
        }
        window.JavaScriptInterface.PageRelocated(cfi, percent);
    });
}

function SelectTextEvent(){
    //Select Text Event
    Rendition.on("selected", function(cfiRange, contents){
        H_cfirange = cfiRange; H_contents = contents;
        N_cfirange = cfiRange; N_contents = contents;

        Book.getRange(cfiRange).then(function(range){
            if(range){
                var buffer = range.toString();
                window.JavaScriptInterface.SelectTextEvent(cfiRange, buffer);
            }
        });
    });
}

function SetBookState(cfi){
    //Add Book State
     Rendition.display(cfi);
}

function SetDictionary(){
    var cfiRange = H_cfirange;
    //Get Text From CFI RANGE
    Book.getRange(cfiRange).then(function(range){
        if(range){
            var buffer = range.toString();
            window.JavaScriptInterface.GetDictionaryData(buffer);
        }
    });
    contents.window.getSelection().removeAllRanges();
}

function GetTOC(){
    Book.loaded.navigation.then(function(toc){
        toc.forEach(function(chapter) {
            var label = chapter.label;
            var href = chapter.href;
            window.JavaScriptInterface.AddTOCData(label, href);
        });
    });
}

function RegisterTheme(){
    Rendition.themes.register("paperthemes", "css/paperthemes.css");
    Rendition.themes.select("paperthemes");

    Rendition.hooks.content.register(this.ApplyTheme.bind(this));
}

function SwipeLeft(){
    //Change Book Page To Next Page
    Rendition.next();
}

function SwipeRight(){
    //Change Book Page To Prev Page
    Rendition.prev();
}

function ChangeFontSize(fSize){
    //in pixel
    fontSize = fSize;

    ApplyTheme();
}

function DisplayMode(val){
    if(val =='1'){
        color = "#000000";
        background = "#ffffff";
    }
    else if(val == '2'){
        color = "#dedede";
        background = "#000000";
    }
    else if(val == '3'){
        color = "#000000";
        background = "#f9f0d6";
    }
    else if(val == '4'){
        color = "#dedede";
        background = "#17406d";
    }
    else if(val == '5'){
        color = "#000000";
        background = "#dcdad7";
    }

    //Change Color and Background
    //Rendition.themes.override("color", color);
    //Rendition.themes.override("background", background);

    ApplyTheme();

    //Display Mode Changed
    window.JavaScriptInterface.DisplayChangedInit(background);
}

function ChangeFont(val){
    if(val == '1'){
        //Roboto
        fontFamily = "FontRoboto";
    }
    else if(val == '2'){
        //Karla
        //fontFamily = "FontKarla";
        fontFamily = "FontSansForgetica";
    }
    else if(val == '3'){
        //Lora
        fontFamily = "FontLora";
    }
    else if(val == '4'){
        //Rubik
        fontFamily = "FontRubik";
    }
    else if(val == '5'){
        //Ubuntu
        fontFamily = "FontUbuntu";
    }
    else if(val == '6'){
        //Nunito
        fontFamily = "FontNunito";
    }

    ApplyTheme();
}

function GotToTitle(url){
    //set url
    Rendition.display(url);
}

function BookmarkPage(){
    var href1 = Rendition.currentLocation().start.href;
    window.JavaScriptInterface.AddBookmarkData(href1);

    //window.JavaScriptInterface.Log(href1);
    //var label = Rendition.currentLocation();
    //var cfi = Rendition.currentLocation().start.cfi;
    //var TOCIndex = Book.spine.get(cfi).index;
    //window.JavaScriptInterface.AddBookmarkData(TOCIndex);
    //var cfi2 = Rendition.currentLocation().end.cfi;
    //var TOCIndex2 = Book.spine.get(cfi2).index;
    //var href2 = Rendition.currentLocation().end.href;
    //window.JavaScriptInterface.Log(TOCIndex + ":::::::::" + TOCIndex2 + ":::::" + href1 + "::::::" + href2);
}

function GoToBookMarkPage(cfi){
    //Goto CFI Page
    Rendition.display(cfi);
}

function GoToNotePage(cfi){
    //Goto CFI Page
    Rendition.display(cfi);
}

function RemoveTag(text, selector) {
    var wrapped = $(text);
    wrapped.find(selector).remove();
    return wrapped.html();
}

function AddHighlightCFI(cfiRange, color){
    //window.JavaScriptInterface.Log(color);
    //Add Highlight
    Rendition.annotations.add("highlight", cfiRange, {}, (e) => { annotateEvent_Click(cfiRange, "highlight");} , "hl", {"fill": color, "fill-opacity": "0.4", "mix-blend-mode": "multiply"});
}

function SetHighlight(color){
    var cfiRange = H_cfirange;
    var contents = H_contents;

    //Remove First can be exists
    Rendition.annotations.remove(cfiRange);

    //Add Highlight
    //window.JavaScriptInterface.Log(color);
    Rendition.annotations.add("highlight", cfiRange, {}, (e) => { annotateEvent_Click(cfiRange, "highlight");} , "hl", {"fill": color, "fill-opacity": "0.4", "mix-blend-mode": "multiply"});

    //Get Text From CFI RANGE
    Book.getRange(cfiRange).then(function(range){
        if(range){
            var buffer = range.toString();
            window.JavaScriptInterface.GetHighlightData(cfiRange, buffer);
        }
    });
    contents.window.getSelection().removeAllRanges();
}

function RemoveHighlight(cfi){
    //Remove Highlight
    Rendition.annotations.remove(cfi);
}

function AddNoteCFI(cfiRange, color){
    //Add Note
    Rendition.annotations.add("highlight", cfiRange, {}, (e) => { annotateEvent_Click(cfiRange, "note");} , "n-hl", {"fill": color, "fill-opacity": "0.4", "mix-blend-mode": "multiply"});
}

function SetNote(note, color){
    var cfiRange = N_cfirange;
    var contents = N_contents;

    //Remove First can be exists
    Rendition.annotations.remove(cfiRange);

    //Add Note
    Rendition.annotations.add("highlight", cfiRange, {}, (e) => { annotateEvent_Click(cfiRange, "note");} , "n-hl", {"fill": color, "fill-opacity": "0.4", "mix-blend-mode": "multiply"});

    //Get Text From CFI RANGE
    Book.getRange(cfiRange).then(function(range){
        if(range){
            var buffer = range.toString();
            window.JavaScriptInterface.GetNoteData(cfiRange, note, buffer);
        }
    });
    contents.window.getSelection().removeAllRanges();
}

function RemoveNote(cfi){
    //Remove Note
    Rendition.annotations.remove(cfi);
}

function SearchInBook_Chapter(query){
    let item = Book.spine.get(Rendition.location.start.cfi);
    var data = item.load(Book.load.bind(Book)).then(item.find.bind(item, query)).finally(item.unload.bind(item));
    data.then(function(response){
        response.forEach(function(element){
            window.JavaScriptInterface.FindSearchedItem(query, element.excerpt, element.cfi);
            //window.JavaScriptInterface.Log("A: " + element.excerpt);
        });
        window.JavaScriptInterface.FinishedSearch();
    });
}

function SearchInBook_EntireBook(query){
    //loop toc
    //Loop All Spine Items Map
    var counter = 0;
    var sItems = Book.spine.spineItems;
    var sTotal = sItems.length;

    sItems.map(item=>{
        var data = item.load(Book.load.bind(Book)).then(item.find.bind(item, query)).finally(item.unload.bind(item));
        //var bookTitle = item.
        data.then(function(response){
            response.forEach(function(element){
                window.JavaScriptInterface.FindSearchedItem(query, element.excerpt, element.cfi);
            });
            //---CHECK HERE
            counter++;
            if(counter >= sTotal){
                window.JavaScriptInterface.FinishedSearch();
            }
        });
    });
}

function GotToSearchCfi(cfi, lastCfi){
    //set cfi
    if(lastCfi != ""){Rendition.annotations.remove(lastCfi);}
    Rendition.display(cfi);
    Rendition.annotations.add("highlight", cfi, {}, (e) => { } , "search-hl", {"fill": "green", "fill-opacity": "0.4", "mix-blend-mode": "multiply"});
}

function RemoveSearchCfi(lastCfi){
    //remove search cfi
    if(lastCfi != ""){Rendition.annotations.remove(lastCfi);}
}

//GET URL PARAMETER
function GetURLParameter(sParam){
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++)
    {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam)
        {
            return sParameterName[1];
        }
    }
}

function checkForAnnotator(cb, w) {
    if (!w) {
        w = window;
    }
    setTimeout(function () {
        if (w && w.annotator) {
            cb();
        } else {
            checkForAnnotator(cb, w);
        }
    }, 100);
}

function RegisterHooks(){
    Rendition.hooks.render.register(function (view){
       //alert("HOOK ADDED");
    });
}

function annotateEvent_Click(cfiRange, type){
    //Annotate Event
    window.JavaScriptInterface.AnnotateEvent_Click(cfiRange, type);
}

function linkClick(){
    //Link Clicked
    window.JavaScriptInterface.LinkClicked();
}

function ApplyTheme(){
    //window.JavaScriptInterface.Log("APPLIED THEME:FFamily->" + fontFamily + ":fontSize->" + fontSize + ":color->" + color + ":back->" + background);
   	let rules = {
       "body": {
           "font-family": fontFamily,
           "background": background,
           "color": color + " !important"
       },
       "p": {
           "color": color + " !important",
           "font-family": fontFamily,
           "font-size": fontSize,
       },
       "a": {
           "color": "inherit !important",
           "text-decoration": "underline",
           "-webkit-text-fill-color": "inherit !important"
       },
       "a:link": {
           "color": "#909090",
           "text-decoration": "underline",
           "-webkit-text-fill-color": "#909090"
       },
       "a:link:hover": {
           "background": "rgba(0, 0, 0, 0.1) !important"
       },
       "img": {
           "max-width": "100% !important"
       },
   };
    if(this.Rendition){
        this.Rendition.getContents().forEach(c => c.addStylesheetRules(rules));
    }
}

//Document Ready
$(document).ready(function(){
    //Init Paper
    var path = GetURLParameter("path");
    var totalCharacter = GetURLParameter("totalCharacter");
    var currentMarginVal = GetURLParameter("currentMarginVal");

    PaperInit(path, totalCharacter, currentMarginVal);
});
