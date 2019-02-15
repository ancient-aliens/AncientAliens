import React from 'react';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Button from '@material-ui/core/Button';
import {GamePhases, Concede, Mulligan, Keep, Pass} from './Game.js';
import './StateDisplay.css';
import './Utils.css';

export class PlayerDisplay extends React.Component {
  render() {
    const {
      name,
      energy,
      maxEnergy,
      knowledgePool,
      deckSize,
      handSize,
      scrapyard,
    } = this.props.player;
    const void_ = this.props.player.void;

    return (
      <Paper className="player-display">
        <Grid container spacing={0}>
          <Grid item xs={12} className="grid-elem">
            {name}
          </Grid>
          <Grid item xs={12} className="grid-elem">
            {energy + '/' + maxEnergy}
          </Grid>
          <Grid item xs={12} className="grid-elem">
            <Grid container spacing={16} justify="space-evenly">
              {knowledgePool.map(knowledge => (
                <Grid item xs={2} key={knowledge.knowledge}>
                  {knowledge.count}
                </Grid>
              ))}
            </Grid>
          </Grid>
          <Grid item xs={6} className="grid-elem">
            {deckSize}
          </Grid>
          <Grid item xs={6} className="grid-elem">
            {handSize}
          </Grid>
          <Grid item xs={6} className="grid-elem">
            {scrapyard.length}
          </Grid>
          <Grid item xs={6} className="grid-elem">
            {void_.length}
          </Grid>
        </Grid>
      </Paper>
    );
  }
}

export class MessageDisplay extends React.Component {
  render() {
    const phase = this.props.phase;
    const amActive = this.props.game.activePlayer === this.props.game.player.id;

    let buttonMenu;
    if (phase !== GamePhases.NOT_STARTED) {
      buttonMenu = (
        <Grid container spacing={0} direction="column" style={{height: '100%'}}>
          <Grid item xs={6} className="grid-col-item no-max-width">
            <Button color="secondary" variant="contained" onClick={Concede}>
              Concede
            </Button>
          </Grid>
          <Grid item xs={6} className="grid-col-item no-max-width">
            <Button
              color="primary"
              variant="contained"
              disabled={!amActive}
              onClick={Pass}>
              Pass
            </Button>
          </Grid>
        </Grid>
      );
    } else {
      buttonMenu = (
        <Grid container spacing={0} direction="column" style={{height: '100%'}}>
          <Grid item xs={6} className="grid-col-item no-max-width">
            <Button color="secondary" variant="contained" onClick={Mulligan}>
              Mulligan
            </Button>
          </Grid>
          <Grid item xs={6} className="grid-col-item no-max-width">
            <Button color="primary" variant="contained" onClick={Keep}>
              Keep
            </Button>
          </Grid>
        </Grid>
      );
    }

    return (
      <Paper className="message-display">
        <Grid container spacing={0} direction="column" style={{height: '100%'}}>
          <Grid item xs={6} className="grid-col-item no-max-width">
            Hi
          </Grid>
          <Grid item xs={6} className="grid-col-item no-max-width">
            {buttonMenu}
          </Grid>
        </Grid>
      </Paper>
    );
  }
}

export default class StateDisplay extends React.Component {
  render() {
    const gary = this.props.game.opponent;
    const me = this.props.game.player;
    const phase = this.props.phase;

    return (
      <Grid
        container
        spacing={24}
        style={{
          padding: 0,
        }}
        className="state-display"
        direction="column">
        <Grid item xs={4} className="grid-col-item no-max-width">
          <PlayerDisplay player={gary} />
        </Grid>
        <Grid item xs={4} className="grid-col-item no-max-width">
          <MessageDisplay game={this.props.game} phase={phase} />
        </Grid>
        <Grid item xs={4} className="grid-col-item no-max-width">
          <PlayerDisplay player={me} />
        </Grid>
      </Grid>
    );
  }
}