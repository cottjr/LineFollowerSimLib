
//----------------------------------
  float x1_h = 0;      // estimated distance from path tangent to robot, inches
  float x2_h= 0;       // estimated angle from path tangent to robot heading, radian
  float errorRate = 0;
  float uErrorRate;
  float uCurvature;
  
  float uError;      //  p component of plant input, robot.steerAngleR
  float theta_pR;
  float b2 = -0.25; // 1 / robot.wheelBase; // element of B matrix (2x1), b1 = 0
  float k1=0.36 , k2=0.846;   // estimator gains, elements of K matrix (2x1)
  float c1_h = 12.8;         // estimator C matrix (1x2), c2_h = 0
  //float c1 = 12.8;           //  C matrix (1x2), c2 = 0
  float g1 = 1.2, g2 = 2.0; // state feedback gains, G matrix (1x2)


void trackingUpdateInit()   // reset low-level controller 
{
  x1_h = 0;      // estimated distance from path tangent to robot, inches
  x2_h= 0;       // estimated angle from path tangent to robot heading, radian
}


void trackingUpdate( int y, float dt)
{
  float dSw = trike.wheelVelocity * dt;
  float dSr = dSw * cos(trike.steerAngleR);
  float y_h = c1_h * x1_h;
  float residual = y - y_h;
  float dx2_h = residual * k2 - b2 * trike.steerAngleR;
  float dx1_h = residual * k1 + x2_h;
  x1_h += dx1_h * dSr;
  x2_h += dx2_h * dSr;
  float u1 = g1 * x1_h;
  float u2 = g2 * x2_h;
  uError = u1;         // used in  DrawWorld
  theta_pR = x2_h;     // used in main?
     trike.steerAngleR = u1 + u2; // = - u
     if (trike.steerAngleR > 1.0 ) trike.steerAngleR = 1.0;
     if (trike.steerAngleR < -1.0 ) trike.steerAngleR = -1.0;
  
     //println (" steer ", trike.steerAngleR);
     
     
     //println("*** %4.2 %4.2 %4.2", sensorRun[0][0], sensorRun[1][0], sensorRun[2][0]);
    // println(c, error, errorRate, delT,"***", sensorRun[1][0] );
} // tracking Update()
//-------------------------------




void trikeDriveUpdate () // called at end of controllerUpdate() 
  {
    // Transform trike driven wheel speed and wheel angle into 
    // forward speed component and rotation rate (degrees/sec) component expected by simulation. 
    // Note: trike is reverse of trike ridden as child  (driven and steered wheel in rear).
    
    lfs.setTargetSpeed(trike.wheelVelocity * cos( trike.steerAngleR ));
    lfs.setTargetTurnRate(degrees( trike.wheelVelocity * sin( trike.steerAngleR ) / trike.wheelBase ));  // CW steer --> CCW turn
  }
