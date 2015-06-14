class Fruit{
  final color col;
  final String name;
  int size;
  PVector position;
  
  color colors[] = new color[3];
  
  public Fruit(PVector p) {
    colors[0] = color(255, 235, 55); //"Citrus"
    colors[1] = color(230, 51, 51); //"Apple"
    colors[2] = color(250, 145, 53); //"Orange"
    size = (int) random(colors.length) + 2;
    col = colors[size - 2];
    switch(size) {
      case 2 : name = "Citron"; break;
      case 3 : name = "Pomme"; break;
      case 4 : name = "Orange"; break;
      default : name = "ERROR"; break;
    }
    this.position = p;
  }
  
  void eaten(){
    size--;
    if(size == 0) {
      cylindersCollection.remove(position.x, position.z);
    }
  }
}
