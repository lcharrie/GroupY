class ShiftBoard {
  final float ratio;

  ShiftBoard(float ratio) {
    this.ratio = ratio;
  }

  void display() {
    // diplay the board
    fill(grassColor);
    rect(-board.size/4, -board.size/4, board.size/2, board.size/2);
    // display the ball
    fill(chenilleColor);
    for(int i = 0; i < ball.chenille.length; i = i + 2) {
      ellipse(ball.chenille[i].x * ratio, ball.chenille[i].z * ratio, ball.radius*2 * ratio, ball.radius*2 * ratio);
    }
    // display the cylinders
    fill(treeColor);
    for (PVector c : cylindersCollection.list) {
      ellipse(c.x * ratio, c.z * ratio, cylindersCollection.radius*2 * ratio, cylindersCollection.radius*2 * ratio);
    }
  }
}

