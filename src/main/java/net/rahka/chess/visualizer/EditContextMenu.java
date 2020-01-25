package net.rahka.chess.visualizer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.Player;

import java.util.function.Consumer;

public class EditContextMenu extends ContextMenu {

    private final ObjectProperty<SpawnAction> spawnActionProperty = new SimpleObjectProperty<SpawnAction>(null);
    public SpawnAction getSpawnAction() {return spawnActionProperty.get();}
    public void setSpawnAction(SpawnAction spawnAction) {spawnActionProperty.set(spawnAction);}
    public ObjectProperty<SpawnAction> spawnActionProperty() {return spawnActionProperty;}

    private final ObjectProperty<KillAction> killActionProperty = new SimpleObjectProperty<KillAction>(null);
    public KillAction getKillAction() {return killActionProperty.get();}
    public void setKillAction(KillAction killAction) {killActionProperty.set(killAction);}
    public ObjectProperty<KillAction> killActionProperty() {return killActionProperty;}

    private final ObjectProperty<ResetAction> resetActionProperty = new SimpleObjectProperty<ResetAction>(null);
    public ResetAction getResetAction() {return resetActionProperty.get();}
    public void setResetAction(ResetAction resetAction) {resetActionProperty.set(resetAction);}
    public ObjectProperty<ResetAction> resetActionProperty() {return resetActionProperty;}

    private final ObjectProperty<KillAllPlayerAction> killAllPlayerProperty = new SimpleObjectProperty<KillAllPlayerAction>(null);
    public KillAllPlayerAction getKillAllPlayer() {return killAllPlayerProperty.get();}
    public void setKillAllPlayer(KillAllPlayerAction killAllPlayerAction) {killAllPlayerProperty.set(killAllPlayerAction);}
    public ObjectProperty<KillAllPlayerAction> killAllPlayerProperty() {return killAllPlayerProperty;}

    private final ObjectProperty<KillAllPiecesAction> killAllPiecesProperty = new SimpleObjectProperty<KillAllPiecesAction>(null);
    public KillAllPiecesAction getKillAllPieces() {return killAllPiecesProperty.get();}
    public void setKillAllPieces(KillAllPiecesAction killAllPiecesAction) {killAllPiecesProperty.set(killAllPiecesAction);}
    public ObjectProperty<KillAllPiecesAction> killAllPiecesProperty() {return killAllPiecesProperty;}

    private final ObjectProperty<ClearAction> clearActionProperty = new SimpleObjectProperty<ClearAction>(null);
    public ClearAction getClearAction() {return clearActionProperty.get();}
    public void setClearAction(ClearAction clearAction) {clearActionProperty.set(clearAction);}
    public ObjectProperty<ClearAction> clearActionProperty() {return clearActionProperty;}

    @NonNull @Getter
    final MenuItem killMenuitem;

    @NonNull @Getter
    final Menu spawnMenu;

    @NonNull @Getter
    final Menu killAllMenu;

    @NonNull @Getter
    final MenuItem resetMenuItem;

    @NonNull @Getter
    final MenuItem clearMenuItem;

    @Setter
    private Piece piece;

    @Setter
    private int x, y;

    public EditContextMenu() {
        super();

        killMenuitem = new MenuItem("Kill");
        killMenuitem.setOnAction((ignored) -> doKillAction());
        killMenuitem.visibleProperty().bind(killActionProperty().isNotNull());

        spawnMenu = new Menu("Spawn");
        spawnMenu.visibleProperty().bind(spawnActionProperty().isNotNull());

        {
            Menu white = new Menu("White");
            white.visibleProperty().bind(spawnActionProperty().isNotNull());
            addPieceActions(white, Player.WHITE, this::doSpawnAction);

            Menu black = new Menu("Black");
            black.visibleProperty().bind(spawnActionProperty().isNotNull());
            addPieceActions(black, Player.BLACK, this::doSpawnAction);

            spawnMenu.getItems().addAll(white, black);
        }

        killAllMenu = new Menu("Kill all");
        {
            MenuItem allWhite = new MenuItem("All whites");
            allWhite.visibleProperty().bind(killAllPlayerProperty().isNotNull());
            allWhite.setOnAction((ignored) -> doKillAllAction(Player.WHITE));

            MenuItem allBlack = new MenuItem("All blacks");
            allBlack.visibleProperty().bind(killAllPlayerProperty().isNotNull());
            allBlack.setOnAction((ignored) -> doKillAllAction(Player.BLACK));

            Menu white = new Menu("White");
            white.visibleProperty().bind(killAllPiecesProperty().isNotNull());
            addPieceActions(white, Player.WHITE, this::doKillAllAction);

            Menu black = new Menu("Black");
            black.visibleProperty().bind(killAllPiecesProperty().isNotNull());
            addPieceActions(black, Player.BLACK, this::doKillAllAction);

            killAllMenu.getItems().addAll(allWhite, allBlack, white, black);

            MenuItem pawn = new MenuItem("Pawns");
            pawn.visibleProperty().bind(killAllPiecesProperty().isNotNull());
            pawn.setOnAction((ignored) -> {
                doKillAllAction(Piece.WHITE_PAWN);
                doKillAllAction(Piece.BLACK_PAWN);
            });

            MenuItem rook = new MenuItem("Rooks");
            rook.visibleProperty().bind(killAllPiecesProperty().isNotNull());
            rook.setOnAction((ignored) -> {
                doKillAllAction(Piece.WHITE_ROOK);
                doKillAllAction(Piece.BLACK_ROOK);
            });

            MenuItem knight = new MenuItem("Knights");
            knight.visibleProperty().bind(killAllPiecesProperty().isNotNull());
            knight.setOnAction((ignored) -> {
                doKillAllAction(Piece.WHITE_KNIGHT);
                doKillAllAction(Piece.BLACK_KNIGHT);
            });

            MenuItem bishop = new MenuItem("Knights");
            bishop.visibleProperty().bind(killAllPiecesProperty().isNotNull());
            bishop.setOnAction((ignored) -> {
                doKillAllAction(Piece.WHITE_BISHOP);
                doKillAllAction(Piece.BLACK_BISHOP);
            });

            MenuItem queens = new MenuItem("Queens");
            queens.visibleProperty().bind(killAllPiecesProperty().isNotNull());
            queens.setOnAction((ignored) -> {
                doKillAllAction(Piece.WHITE_QUEEN);
                doKillAllAction(Piece.BLACK_QUEEN);
            });

            MenuItem king = new MenuItem("Kings");
            king.visibleProperty().bind(killAllPiecesProperty().isNotNull());
            king.setOnAction((ignored) -> {
                doKillAllAction(Piece.WHITE_KING);
                doKillAllAction(Piece.BLACK_KING);
            });

            killAllMenu.getItems().addAll(pawn, rook, knight, bishop, queens, king);
        }

        resetMenuItem = new MenuItem("Reset");
        resetMenuItem.visibleProperty().bind(resetActionProperty().isNotNull());
        resetMenuItem.setOnAction(this::doResetAction);

        clearMenuItem = new MenuItem("Clear");
        clearMenuItem.visibleProperty().bind(clearActionProperty().isNotNull());
        clearMenuItem.setOnAction(this::doClearAction);

        getItems().addAll(killMenuitem, spawnMenu, killAllMenu, resetMenuItem, clearMenuItem);
    }

    public void show(Node anchor, double screenX, double screenY, Piece piece, int boardX, int boardY) {
        setX(boardX);
        setY(boardY);
        setPiece(piece);

        show(anchor, screenX, screenY);
    }

    private void addPieceActions(Menu parent, Player player, Consumer<Piece> action) {
        for (Piece piece : Piece.of(player)) {
            String name = piece.toString().substring(piece.toString().indexOf("_") + 1).toLowerCase();
            name = name.substring(0, 1).toUpperCase() + name.substring(1);

            MenuItem item = new MenuItem(name);
            item.setOnAction((ignored) -> action.accept(piece));
            parent.getItems().add(item);
        }
    }

    private void doKillAction() {
        var action = getKillAction();
        if (action != null) {
            action.kill(piece, x, y);
        }
    }

    private void doKillAllAction(Player player) {
        var action = getKillAllPlayer();
        if (action != null) {
            action.killAll(player);
        }
    }

    private void doKillAllAction(Piece piece) {
        var action = getKillAllPieces();
        if (action != null) {
            action.killAll(piece);
        }
    }

    public void doSpawnAction(Piece piece) {
        var action = getSpawnAction();
        if (action != null) {
            action.spawn(piece, x, y);
        }
    }

    public void doResetAction(ActionEvent ignored) {
        var action = getResetAction();
        if (action != null) {
            action.reset();
        }
    }

    public void doClearAction(ActionEvent ignored) {
        var action = getClearAction();
        if (action != null) {
            action.clear();
        }
    }

    public interface SpawnAction {

        void spawn(Piece piece, int x, int y);

    }

    public interface KillAction {

        void kill(Piece piece, int x, int y);

    }

    public interface KillAllPiecesAction {

        void killAll(Piece piece);

    }

    public interface KillAllPlayerAction {

        void killAll(Player player);

    }

    public interface ResetAction {

        void reset();

    }

    public interface ClearAction {

        void clear();

    }

}
