void setup () {
  size(1000,1000,P2D);
}

float scale = 4.0;
float rotX = 0.0;
float rotY = 0.0;
My3DBox input3DBox;

void draw() {
  background(255, 255, 255);
  My3DPoint eye = new My3DPoint(0, 0, -5000);
  My3DPoint origin = new My3DPoint(50, 50, 50);
  My3DBox input3DBox = new My3DBox(origin, 100, 150, 200);

  //scaled
  float[][] transform1 = scaleMatrix(scale, scale, scale);
  input3DBox = transformBox(input3DBox, transform1);
  //rotated around x
  float[][] transform2 = rotateXMatrix(rotX);
  input3DBox = transformBox(input3DBox, transform2);
    //rotated around y
  float[][] transform3 = rotateYMatrix(rotY);
  input3DBox = transformBox(input3DBox, transform3);
  
  //rendu 3D
  projectBox(eye, input3DBox).render();
}

void keyPressed() {
  if (key == CODED) {
    switch(keyCode) {
      case UP:
        rotX += 0.05;
        break;
      case DOWN:
        rotX -= 0.05;
        break;
      case LEFT:
        rotY += 0.05;
        break;
      case RIGHT:
        rotY -= 0.05;
        break;
      default:
        break;
    }
  }
}

void mouseDragged(){
  scale = max(0, scale + (mouseY - pmouseY)/50.0);
}

class My2DPoint {
  float x;
  float y;
  My2DPoint(float x, float y) {
    this.x = x;
    this.y = y;
  }
}

class My3DPoint {
  float x;
  float y;
  float z;
  My3DPoint(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
}

My2DPoint projectPoint(My3DPoint eye, My3DPoint p) {
  float xProj = (eye.z*(p.x-eye.x))/(eye.z-p.z);
  float yProj = (eye.z*(p.y-eye.y))/(eye.z-p.z);
  return new My2DPoint(xProj, yProj);
}

class My2DBox {
  My2DPoint[] s;
  My2DBox(My2DPoint[] s) {
    this.s = s;
  }
  void render(){
    strokeWeight(4);
    for(int i = 0; i<4; i++){
      stroke(0,255,0);
      line(s[i+4].x, s[i+4].y, s[(i+1)%4+4].x, s[(i+1)%4+4].y);
    }
    for(int i = 0; i<4; i++){
      stroke(255,0,0);
      line(s[i].x, s[i].y, s[(i+1)%4].x, s[(i+1)%4].y);
    }
    for(int i = 0; i<4; i++){
      stroke(0,0,255);
      line(s[i].x, s[i].y, s[i+4].x, s[i+4].y);
    }
  }
}

class My3DBox {
  My3DPoint[] p;
  My3DBox(My3DPoint origin, float dimX, float dimY, float dimZ){
  float x = origin.x;
  float y = origin.y;
  float z = origin.z;
  this.p = new My3DPoint[]{new My3DPoint(x,y+dimY,z+dimZ), new My3DPoint(x,y,z+dimZ), new My3DPoint(x+dimX,y,z+dimZ), 
                           new My3DPoint(x+dimX,y+dimY,z+dimZ), new My3DPoint(x,y+dimY,z), origin,
                           new My3DPoint(x+dimX,y,z), new My3DPoint(x+dimX,y+dimY,z)
                          };
  }
  My3DBox(My3DPoint[] p) {
    this.p = p;
  }
}

My2DBox projectBox (My3DPoint eye, My3DBox box) {
  My2DPoint[] s = new My2DPoint[box.p.length];
  for(int i=0; i<8; i++){
    s[i] = projectPoint(eye, box.p[i]);
  }
  return new My2DBox(s);
}

float[] homogeneous3DPoint (My3DPoint p) {
  float[] result = {p.x, p.y, p.z , 1};
  return result;
}

float[][] rotateXMatrix(float angle) {
  return(new float[][] {{1, 0 , 0 , 0},
                        {0, cos(angle), -sin(angle) , 0},
                        {0, sin(angle) , cos(angle) , 0},
                        {0, 0 , 0 , 1}
                      });
}
float[][] rotateYMatrix(float angle) {
  return(new float[][] {{cos(angle), 0 , sin(angle) , 0},
                        {0, 1, 0 , 0},
                        {-sin(angle), 0 , cos(angle) , 0},
                        {0, 0 , 0 , 1}
                      });
}
float[][] rotateZMatrix(float angle) {
  return(new float[][] {{cos(angle), -sin(angle) , 0 , 0},
                        {sin(angle), cos(angle) , 0 , 0},
                        {0, 0 , 1 , 0},
                        {0, 0 , 0 , 1}
                      });
}
float[][] scaleMatrix(float x, float y, float z) {
  return(new float[][] {{x, 0 , 0 , 0},
                        {0, y , 0 , 0},
                        {0, 0 , z , 0},
                        {0, 0 , 0 , 1}
                      });
}
float[][] translationMatrix(float x, float y, float z) {
  return(new float[][] {{1, 0 , 0 , 0},
                        {0, 1 , 0 , 0},
                        {0, 0 , 1 , 0},
                        {x, y , z , 1}
                      });
}

float[] matrixProduct(float[][] a, float[] b) {
  float[] prodMat = new float[b.length];
  for (int i=0; i<a.length; i++){ 
    float n = 0;
    for (int j=0; j<b.length; j++){
      n = n + a[j][i]*b[j];
    }
    prodMat[i] = n;
  }
  return prodMat;
}

My3DBox transformBox(My3DBox box, float[][] transformMatrix) {
  My3DPoint[] newP = new My3DPoint[box.p.length];
  for (int i=0; i<box.p.length; i++){
    newP[i] = euclidian3DPoint(matrixProduct(transformMatrix, homogeneous3DPoint(box.p[i])));
  }
  return new My3DBox(newP);
}

My3DPoint euclidian3DPoint (float[] a) {
  My3DPoint result = new My3DPoint(a[0]/a[3], a[1]/a[3], a[2]/a[3]);
  return result;
}
