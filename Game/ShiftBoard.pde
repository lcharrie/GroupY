class ShiftBoard {
  final float ratio;

  ShiftBoard(float ratio) {
    this.ratio = ratio;
  }

  void display() {
    // diplay the board
    fill(grassColor);
    rect(-board.size/2 * ratio, -board.size/2 * ratio, board.size * ratio, board.size * ratio);
    // display the ball
    fill(ballColor);
    ellipse(ball.location.x * ratio, ball.location.z * ratio, ball.radius*2 * ratio, ball.radius*2 * ratio);
    // display the cylinders
    fill(treeColor);
    for (PVector c : cylindersCollection.list) {
      ellipse(c.x * ratio, c.z * ratio, cylindersCollection.radius*2 * ratio, cylindersCollection.radius*2 * ratio);
    }
  }
}

