package info.tehnut.csgo.presence;

import club.minnced.discord.rpc.DiscordRPC;
import info.tehnut.csgo.gamestate.IStateUpdateWatcher;
import info.tehnut.csgo.gamestate.data.GameMap;
import info.tehnut.csgo.gamestate.data.GameState;
import info.tehnut.csgo.gamestate.data.MatchStats;

public class UpdateWatcher implements IStateUpdateWatcher {

    public void handleUpdatedState(GameState newState) {
        if (newState.getMap() != null) { // We're in a match of some kind
            GameMap map = newState.getMap();
            CSGOPresence.DISCORD_PRESENCE.largeImageText = "Map: " + map.getName();

            GameModes gameMode;
            if (TrayHandler.tryFindSurf && map.getName().startsWith("surf_")) {
                gameMode = GameModes.SURF;
                CSGOPresence.DISCORD_PRESENCE.details = "Surf";
                CSGOPresence.DISCORD_PRESENCE.largeImageKey = "surf_map";
            } else {
                gameMode = GameModes.getbyName(map.getMode());
                CSGOPresence.DISCORD_PRESENCE.details = gameMode.getDisplayName(); // deathmatch -> Deathmatch

                if (!newState.isOurUser())
                    CSGOPresence.DISCORD_PRESENCE.details += " (Spectating)";

                String imageName = CSGOPresence.MAP_IMAGES.getOrDefault(map.getName(), "default");
                if (imageName.isEmpty())
                    imageName = map.getName();
                CSGOPresence.DISCORD_PRESENCE.largeImageKey = imageName;
            }

            CSGOPresence.DISCORD_PRESENCE.state = gameMode.getScoreHandler().getScore(newState.getPlayer(), map);

            if (!newState.isOurUser())
                return;

            CSGOPresence.DISCORD_PRESENCE.smallImageKey = "sdefault";
            MatchStats matchStats = newState.getPlayer().getMatchStats();
            CSGOPresence.DISCORD_PRESENCE.smallImageText = String.format("%dK / %dA / %dD", matchStats.getKills(), matchStats.getAssists(), matchStats.getDeaths());
        } else { // We're not in a match. Must be in the main menu... or at least a menu of some sort... probably
            CSGOPresence.DISCORD_PRESENCE.details = "In menu";
            CSGOPresence.DISCORD_PRESENCE.state = null;
            CSGOPresence.DISCORD_PRESENCE.largeImageKey = "default";
            CSGOPresence.DISCORD_PRESENCE.largeImageText = null;
            CSGOPresence.DISCORD_PRESENCE.smallImageKey = null;
            CSGOPresence.DISCORD_PRESENCE.smallImageText = null;
        }

        DiscordRPC.INSTANCE.Discord_UpdatePresence(CSGOPresence.DISCORD_PRESENCE);
    }
}
