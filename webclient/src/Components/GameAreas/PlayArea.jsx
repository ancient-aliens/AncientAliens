import React, { Component } from "react";
import { DragDropContext } from "react-dnd";
import MultiBackend from "react-dnd-multi-backend";
import HTML5toTouch from "react-dnd-multi-backend/lib/HTML5toTouch";
import Grid from "@material-ui/core/Grid";
import { connect } from "react-redux";

import Board from "./Board";
import Stack from "./Stack";
import Altar from "./Altar";
import ResourceArea from "./ResourceArea";
import StateDisplay from "./StateDisplay";

import ChooseDialog from "../Dialogs/ChooseDialog";
import LoadingDialog from "../Dialogs/LoadingDialog";
import WinLoseDialog from "../Dialogs/WinLoseDialog";
import SelectXDialog from "../Dialogs/SelectXDialog";
import { withHandlers } from "../MessageHandlers/HandlerContext";
import { mapDispatchToProps } from "../Redux/Store";
import { GamePhases } from "../Helpers/Constants";
import proto from "../../protojs/compiled";

import "../../css/App.css";


const mapStateToProps = state => {
  return {
    phase: state.extendedGameState.phase,
    game: state.extendedGameState.game
  };
};

class PlayArea extends Component {

  componentDidUpdate(prevProps, prevState, snapshot) {
    let gameHandler = this.props.gameHandler;
    let game = this.props.game;
    let phase = this.props.phase;

    //Auto keep function
    if (
      game.phase === proto.Phase.MULLIGAN &&
      game.activePlayer === game.player.name &&
      game.player.hand.length === 0
    ) {
      gameHandler.Keep();
    }

    //Auto pass function
    if (
      game.phase !== proto.Phase.MULLIGAN &&
      phase === GamePhases.UPDATE_GAME &&
      game.activePlayer === game.player.name &&
      game.canStudy.length === 0 &&
      game.canActivate.length === 0 &&
      game.canPlay.length === 0
    ) {
      setTimeout(function() {
        gameHandler.Pass();
      }, 1000);
      this.props.updateExtendedGameState({ autoPass: true });
    } else {
      this.props.updateExtendedGameState({ autoPass: false });
    }
  }

  render() {
    const { phase, game } = this.props;
    const hasStudyable =
      phase === GamePhases.UPDATE_GAME &&
      game.activePlayer === game.player.name &&
      game.canStudy.length > 0;

    return (
      <div className="App" >
        <header className="App-header" >
          <WinLoseDialog />
          <LoadingDialog />
          <SelectXDialog />
          <ChooseDialog />
          <Grid
            container
            spacing={8}
            style={{
              padding: "12px 12px",
              height: "100vh"
            }}
            justify="space-between"
          >
            <Grid item xs={2} className="display-col">
              <StateDisplay />
            </Grid>
            <Grid item xs={8} className="display-col">
              <Board/>
            </Grid>
            <Grid item xs={2} className="display-col">
              <Grid
                container
                spacing={8}
                justify="center"
                alignItems="center"
                style={{
                  height: "100%"
                }}
              >
                <Grid item xs={12} style={{ height: "10%" }}>
                  <ResourceArea isPlayer={false} />
                </Grid>
                <Grid item xs={12} style={{ height: (hasStudyable? "50%" : "65%") }}>
                  <Stack />
                </Grid>
                <Grid item xs={12} style={{ height: "10%" }}>
                  <ResourceArea isPlayer={true} />
                </Grid>
                <Grid item xs={12} style={{ height: (hasStudyable ? "30%" : "15%") }}>
                  <Altar />
                </Grid>
              </Grid>
            </Grid>
          </Grid>
        </header>
      </div>
    );
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(DragDropContext(MultiBackend(HTML5toTouch))(withHandlers(PlayArea)));
