class Board {
  final float size;
  final float thickness;
  
  Board(float size, float thickness) {
    this.size = size;
    this.thickness = thickness;
  }
  void display() {
    fill(grassColor);
    box(size, thickness, size);
  }
}

