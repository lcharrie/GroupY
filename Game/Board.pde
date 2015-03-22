class Board {
  final int sizeX;
  final int sizeY;
  final int sizeZ;
  Board(int x, int y, int z) {
    sizeX = x;
    sizeY = y;
    sizeZ = z;
  }
  void display() {
    fill(0,102,0);
    box(sizeX, sizeY, sizeZ);
  }
}

