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
    if(!fly) {
      for(int i = 0; i < ball.chenille.length; i = i + 2) {
        ellipse(ball.chenille[i].x * ratio, ball.chenille[i].z * ratio, ball.radius*2 * ratio, ball.radius*2 * ratio);
      }
    }
    // display the cylinders
    for (int i = 0; i < cylindersCollection.list.size(); ++i) {
      PVector c = cylindersCollection.list.get(i);
      fill(cylindersCollection.fruits.get(i).col);
      ellipse(c.x * ratio, c.z * ratio, cylindersCollection.fruits.get(i).size * cylindersCollection.radius * ratio, cylindersCollection.fruits.get(i).size * cylindersCollection.radius * ratio);
    }
    PVector p = montgolfiere.getPosition();
    fill(0, 0, 255);
    ellipse(p.x * ratio, p.z * ratio, montgolfiere.size * 4 * ratio, montgolfiere.size * 4 * ratio);
  }
}

