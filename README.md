# CamHead

A Minecraft Plugin to add cameras to your server.

## Components

- Room: A room is used to store screens and cameras.
- Camera: A camera is an animated block that allows players to watch the room.
- Screen: A screen is a block used to enter the cameras.

When entering a screen (right click), you are teleported to the camera.

You can sneak to leave the camera, or click to change the camera.

Currently, you have to use the command `/camhead add` to add a camera/screen.

To remove a camera/screen you can use the command `/camhead remove` or you can break the block to get the command in chat.

## Commands

All commands work with `/execute ... run ...`.

The plugin is reloadable, which means `/reload` won't break it (but avoid using /reload, use `/camhead reload` instead).

Selectors:
- `\<room\>` is a selector with the name of the room (i.e. `room1`).
- `\<camera\>` is a selector with the name of the room followed by the name of the camera (i.e. `room1 camera1`).
- `\<screen\>` is a selector with the name of the room followed by the name of the screen (i.e. `room1 screen1`).
- `\<component\>` is a selector for a room, a screen or a camera.

| Command                                                                              | Description                              |
| ------------------------------------------------------------------------------------ | ---------------------------------------- |
| `/camhead add [room/camera/screen] <component> (x y z)`                              | Creates a component.                     |
| `/camhead info [room/camera/screen] <component>`                                     | Shows information about a component.     |
| `/camhead leave`                                                                     | Leave if in a camera.                    |
| `/camhead list [room/camera/screen] (\<room\>)`                                      | List all components.                     |
| `/camhead move [room/camera/screen] <component> (x y z)`                             | Move the location of the component       |
| `/camhead reload (<room>)`                                                           | Save and Reload the plugin; or the room. |
| `/camhead remove [room/camera/screen] <component>`                                   | Removes a component.                     |
| `/camhead replace [camera/screen] <component>`                                       | Replace the block of the component       |
| `/camhead rotate [camera/screen] <component> <support direction> <facing direction>` | Rotate the block of the component        |
| `/camhead spectate <camera>`                                                         | Enter the camera.                        |
| `/camhead tpto <component>`                                                          | Teleport to the component.               |

