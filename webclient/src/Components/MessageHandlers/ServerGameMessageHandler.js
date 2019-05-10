import { GameProtoSocket } from "../../protojs/ProtoSocket";
import { GamePhases, toGamePhase, toSelectFromType } from "../Constants/Constants";
import { GetGameURL } from "../../Utils";

export default class ServerGameMessageHandler {
  constructor(username, gameId, updateExtendedGameState) {
    this.username = username;
    this.updateExtendedGameState = updateExtendedGameState;
    this.gameId = gameId;
    this.protoSocket = new GameProtoSocket(GetGameURL(username, gameId), this.handleMsg);
  }

  //This is a message handler for ServerGameMessage messages
  handleMsg = (msgType, params) => {
    // XXX Remember to update this with the protocol updates
    let newState = {};
    newState["phase"] = toGamePhase(msgType, params.messageType);
    switch (msgType) {
      case "SelectFrom":
        newState["selectableCards"] = params.canSelected;
        newState["selectablePlayers"] = params.canSelectedPlayers;
        newState["upTo"] = params.upTo;
        newState["selectCountMax"] = params.selectionCount;
        if (
          newState["phase"] === GamePhases.SELECT_FROM_LIST ||
          newState["phase"] === GamePhases.SELECT_FROM_SCRAPYARD ||
          newState["phase"] === GamePhases.SELECT_FROM_VOID
        ) {
          newState["dialog"] = {
            open: true,
            title: `Select ${params.selectionCount} from the following`,
            cards: params.candidates
          };
        }
        break;
      case "GameEnd":
        newState["win"] = params.win;
        break;
      case "OrderCards":
        newState["dialog"] = {
          open: true,
          title: "Order following cards",
          cards: params.cardsToOrder
        };
        break;
      case "SelectXValue":
        newState["maxXValue"] = params.maxXValue;
        break;
      default:
        break;
    }
    newState["game"] = params.game;
    newState["selectedCards"] = [];

    this.updateExtendedGameState(newState);
  };

  send = (msgType, params) => {
    this.protoSocket.send(msgType, params);
  };

  Pass = () => {
    this.send("Pass", { gameID: this.gameId, username: this.username });
  };

  Mulligan = () => {
    this.send("Mulligan", {
      gameID: this.gameId,
      username: this.username
    });
  };

  Keep = () => {
    this.send("Keep", {
      gameID: this.gameId,
      username: this.username
    });
  };

  Concede = () => {
    this.send("Concede", {
      gameID: this.gameId,
      username: this.username
    });
  };

  PlayCard = cardID => {
    this.send("PlayCard", {
      gameID: this.gameId,
      username: this.username,
      cardID: cardID
    });
  };

  ActivateCard = cardID => {
    this.send("ActivateCard", {
      gameID: this.gameId,
      username: this.username,
      cardID: cardID
    });
  };

  StudyCard = cardID => {
    this.send("StudyCard", {
      gameID: this.gameId,
      username: this.username,
      cardID: cardID
    });
  };

  SelectDone = (phase, selectedCards) => {
    this.send("SelectFromResponse", {
      gameID: this.gameId,
      messageType: toSelectFromType(phase),
      selectedCards: selectedCards
    });
    this.updateExtendedGameState({
      phase: GamePhases.DONE_SELECT,
      selectedCards: [],
      selectableCards: [],
      dialog: { text: "", open: false, cards: [] }
    });
  };
  
  SelectXValue = xVal => {
    this.send("SelectXValueResponse", {
      gameID: this.gameId,
      selectedXValue: xVal
    });
    this.props.updateExtendedGameState({ phase: GamePhases.DONE_SELECT });
  };
}