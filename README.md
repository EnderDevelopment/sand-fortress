# Minecraft Plugin: Piaskowa Twierdza

Piaskowa Twierdza is a Minecraft plugin for Paper servers that creates temporary, protected sand castles when a player places a sand block. The plugin applies Strength II to the player who activates the castle for 30 seconds, builds a 10 by 10 sand castle, teleports nearby players inside, prevents the castle blocks from being broken or moved, and removes the structure after 30 seconds.

## Install

1. Build the plugin with Java 21 and Maven:

   `mvn package`

2. Copy the generated `target/plugin.jar` file to your server's `plugins` directory.

3. Start or restart a Paper 1.21.11 server.

4. Confirm that the plugin has loaded successfully in the server console.

## Use

1. Join the server with a player.
2. Place a sand block.
3. A 10 by 10 sand castle is created around the placed block.
4. The player who placed the block receives Strength II for 30 seconds.
5. Players within 10 blocks of the castle center are teleported inside the castle.
6. The generated castle blocks cannot be broken, destroyed by explosions, or moved by pistons.
7. The castle and its generated blocks disappear after 30 seconds.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/acc3eb9f-aca5-4a60-a80d-6df2b3f38289?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=acc3eb9f-aca5-4a60-a80d-6df2b3f38289
