Visual Computing - Milestone 1
=======

Group Y : Charrière Loïc, Poma FLorian, Unternaehrer Charline, Haizmann Jean
-----------
 
### Projections.pde

* When running the sketch Projections.pde, it displays 3 cubes on the screen, each of the rotate, scale,
and translate transformation are visible (like the figure on the last page of assignment #2)
* Projections.pde implements these transformations and the projections in P2D rendering mode and using 
matrix multiplications (no use of Processing built-in transformation functions)

### InteractiveProjection.pde

* When running Processing sketch InteractiveProjection.pde, a cuboid appears at the center of the
screen.
* Up and Down keys must rotate the cuboid around its X axis.
* Right and Left keys must rotate the cuboid around its Y axis.

### Game.pde

* A Processing sketch named Game.pde displays a 3D ’board’ at the center of the screen, with a ball (3D
sphere) on it.
* Mouse drag tilts a board around the X and Z axis.
* Mouse wheel increases/decreases the tilt motion speed.
* When the board is tilted, the ball moves according to the gravity and friction (gravity points toward +Y).
* By pressing the Shift key, a top view of the board is displayed (object placement mode). In this mode, a
click on the board surface adds a new cylinder at click’s location.
* These cylinders remain on the board when the Shift key is released, and move with the board when it is
tilted with the mouse.
* The ball collides with the cylinders and board’s edges. The correct collision distance is computed.
* When colliding with a cylinder or hitting the edges of the board, the ball makes a realistic bounce (by
realistic we mean: correct bounce direction + parabolic motion)