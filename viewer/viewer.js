var _currentCommands = []
const colors = ["magenta", "green", "yellow", "orange", "brown", "cyan", "gray", "crimson", "olive"]

function runClaim(claim) {
  if (claim == undefined) return
  cy.elements('edge[source = "' + claim.source +'"][target = "' + claim.target +'"]')
  .style({
    "line-color": colors[claim.punter]
  })

}

function runMove(move) {
  if (move == undefined) return
  move.moves.forEach( m => runClaim(m.claim))  

}


function loadCommands(commands) {
  _currentCommands = commands

  // REload map
  commands.forEach( command => {
    if (command.map != undefined) {
      if (cy.elements !== undefined) {
        cy.destroy();
      }
      initCy(command.map, function () {
        cy.autolock(true)
        cy.edges().on("select", function(evt) { cy.edges().unselect() } );
      } ); 
    }
  })

  commands.forEach( command => {
    runClaim(command.claim)
    runMove(command.move)
  })


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

