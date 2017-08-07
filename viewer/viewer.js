var _currentCommands = []
const colors = ["magenta", "green", "yellow", "orange", "brown", "cyan", "gray", "crimson", "olive"]

function runClaim(claim) {
  if (claim == undefined) return
  console.log("running claim on" + JSON.stringify(claim))
  selector='edge[source = "' + claim.source +'"][target = "' + claim.target +'"]'
  edges = cy.edges(selector)
  edges.style({
    "line-color": colors[claim.punter]
  })
}

function runClaimAnimaed(claim) {
  if (claim == undefined) return
  console.log("running claim on" + JSON.stringify(claim))
  selector='edge[source = "' + claim.source +'"][target = "' + claim.target +'"]'
  edges = cy.edges(selector)
  edges.animate({style:{
    "width": 500,
    "line-color": colors[claim.punter]
  }})
  setTimeout(function() { 
   edges.animate({style:{
    "width": 10,
    "line-color": colors[claim.punter]
  }})
  }, 300);
}

function runUnClaim(claim) {
  if (claim == undefined) return
  console.log("running unclaim on" + JSON.stringify(claim))
  selector='edge[source = "' + claim.source +'"][target = "' + claim.target +'"]'
  console.log("selector is '" + selector + "'")
  edges = cy.edges(selector)
  edges.animate({style:{
    "width": 500,
    "line-color": "yellow"
  }})

  setTimeout(function() { 
   edges.animate({style:{
    "width": 10,
    "line-color": "#009"
  }})
  }, 300);
}


function runMove(move) {
  if (move == undefined) return
    move.moves.forEach( m => runClaim(m.claim))  

}

var _cachedClaimCommands = []
var _cachedMapCommand = undefined
var _currentCommand = 0
function moveCommands() {
  return _cachedClaimCommands
}

function prevCommand() {
  console.log("Prev command")
  if (_currentCommand <= 0 ) return

  runUnClaim(_cachedClaimCommands[_currentCommand])
  _currentCommand--
}

function nextCommand() {
  console.log("Next command")
  if (_currentCommand>=_cachedClaimCommands.length - 1) return
  _currentCommand++
  runClaimAnimaed(_cachedClaimCommands[_currentCommand])
}


function reloadMoveCommands(count) {
  part = _cachedClaimCommands.slice(0, count)
  reloadMap()
  console.log("Reloading " + part.length + " commands " + " of " + _cachedClaimCommands.length)
  part.forEach( command => {
    runClaim(command)
  })
  _currentCommand = part.length - 1 
}

function reloadMap() {
  cy.destroy();
  initCy(_cachedMapCommand.map, function () {
    // cy.autolock(true)
    cy.edges().on("select", function(evt) { cy.edges().unselect() } );
  }); 
}

function loadCommands(commands) {
  _currentCommands = commands

  commands.forEach( command => {
    if (command.map != undefined) {
      if (cy.elements !== undefined) {
        cy.destroy();
      }
      initCy(command.map, function () {
        // cy.autolock(true)
        cy.edges().on("select", function(evt) { cy.edges().unselect() } );
      } ); 
      _cachedMapCommand = command
    }

    if (command.claim != undefined) {
      _cachedClaimCommands.push(command.claim)
     }
  })

  commands.forEach( command => {
    runClaim(command.claim)
    runMove(command.move)
  })

  _currentCommand = _cachedClaimCommands.length -1
  console.log("Cached " + _cachedClaimCommands.length)


}

function loadMapList(showFirst) {
  fetch("maps.json", {mode: "no-cors"})
  .then(function(res) {
    return res.json() })
  .then(function(json) {
    const select_elem = $("#maps-select");
    const maps = json.maps;

    for (let i = 0; i < maps.length; i++) {
      const map = maps[i];
      const opt = new Option(map.name + " (" + map.num_nodes + " sites and " + map.num_edges + " rivers )", map.filename);
      select_elem.append(opt);
    }

    select_elem.change(function(evt) {
      const item = select_elem.find(":selected");
      //alert("selected " + item.text() + ", val: " + item.val());
      selectMap(item.val());
    } );

    if (showFirst) {
      selectMap(maps[0].filename);
    }
  });

}

function selectMap(url) {
  fetch(url, {mode: "no-cors"})
  .then(function(res) {
    return res.json()
  }).then(function(json) {
    if (cy.elements !== undefined) {
      cy.destroy();
    }
    initCy(json, function () {
      cy.autolock(true)
      cy.edges().on("select", function(evt) { cy.edges().unselect() } );
    } );
  });

  $("#download-link").attr("href", url);
}


$(function(){
  const matches = /map=([^&#=]*)/.exec(window.location.search);
  if (matches !== null && matches !== undefined) {
    const param1 = matches[1];
    loadMapList(false);
    selectMap(param1);
  } else {
    loadMapList(false);
  }
})

