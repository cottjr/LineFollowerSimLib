package LineFollowerSim;

import processing.core.*;

/** Create linear array of spot sensors, no public constructor for this class, LFS.createLineSensor() method is used instead. 
 * 
 * @author Ron Grant 
 *
 */
public class LineSensor extends SpotSensor {
  
  private Sensors sensors;  // object that performs operations on Line and Spot sensors
                            // keeps track of lists of each ...
                          
  private int sensorCells;          // cells (spots) in line sensor   assumed to be spotWPix x spotHPix each with zero space beteween each sensor pixel
  
  float[] sensorTable;  

  // modifiers - set after lines sensor created 

  /** default is 0 (straight line) non zero values used for 1/2 circle, see setArcRadius method.
   *  This value can be dynamically modified at each simulation step.
   * 
   */
  public float arcRadius;           
  /*
   *  Sensor rotation, see setRotation method. This value can be dynamically modified at each simulation step.
   */
  public float rotationAngle;       // rotate sensor arrary cw degrees  
   
  /**
   * flag that is currently not used. Might be a hint for a useful feature,
   */
  public boolean noFeatures;       
                                   
  
  
/**
 * 
 * @param sensors     Reference to Sensors class instance which maintains lists of spot and/or line sensors.
 * @param xoff		  Offset (inches) from robot center in robot coordinates x direction, +X forward toward front of robot 
 * @param yoff        Offset (inches) from robot center in robot coordinates y direction, +Y to right side of robot
 * @param spotWPix    Width of sensor spot  in pixels 
 * @param spotHPix	  Height of sensor spot in pixels
 * @param sensorCells Number of cells (spots) in line sensor. If even value is specified, it will be incremented to 
 *                    create an odd number of spots where one spot will be located at the sensor center located in 
 *                    robot coordinates (xoff,yoff).
 *
 *                    
 */
  LineSensor (PApplet parent, Sensors sensors, float xoff, float yoff, int spotWPix, int spotHPix, int sensorCells)
  {
	super(parent,sensors,xoff,yoff,spotWPix,spotHPix);  
	  
    this.sensors = sensors;
    
    this.xoff = xoff;
    this.yoff = yoff;
    this.spotWPix = spotWPix;
    this.spotHPix = spotHPix;
    
    arcRadius = 0.0f;
    rotationAngle = 0.0f; 
       
    this.sensorCells = sensorCells;
    sensorTable = new float[sensorCells];  // allocate sensor values - use read() or getSensorTable() to access
    
    sensors.lineSensorList.add(this); 
    
  }

  /**
   * 
   * @return number of sensor spots (cells) in this line sensor.
   */
  public int getSensorCellCount () { return sensorCells; }
  
  /**  Read sensor data acquired from sampling robot view image.
   *   
   * @return float[] sized to number sensor cells (spots). 
   */
  public float[] readArray() { return sensorTable; }             


  /**
   * Get line sensor center spot X offset (inches) from robot center, in robot coordinates, specified in LFS.createSpotSensor() 
   *
   */
  public float getXoff() {return xoff; }

  /**
   * Get line sensor center spot Y offset (inches) from robot center, in robot coordinates, specified in LFS.createSpotSensor() 
   *
   */
  public float getYoff() {return yoff; }
  /** Get spot width used in all spots of line sensor in pixels. 
   *
  */
  public int   getSpotWPix() { return spotWPix; }
  /** Get spot height used in all spots of line sensor in pixels. 
   * 
   */
  public int   getSpotHPix() { return spotHPix; }
  /**
   * Set line sensor center at new xoff position in robot coordinates.
   */
  public void setXoff(float xoff) {this.xoff=xoff;}
  /**
   * Set line sensor center at new yoff position in robot coordinates.
   */
  public void setYoff(float yoff) {this.yoff=yoff;}

  
 

  /**When set to non-zero value, line sensor will become a 1/2 circle sensor with given radius.
   * The circle will be centered at the sensor xoff,yoff location and the sensor will project forward unless
   * a negative radius is specified in which case the arc will project backwards more like a "U" shape.
   * 
   * @param radiusInches Radius of 1/2 circle sensor arc. If zero, sensor is a straight line.
   */
  public void setArcRadius(float radiusInches) { arcRadius = radiusInches; }      // when non-zero array is 1/2 circle, radius inches
  
  /**
   * Rotate line (or 1/2 circle) sensor by given angle in degrees, positive angle rotate clockwise,
   * negative angle, counter-clockwise.  
   * 
   * @param angleDegrees Rotation angle (degrees)
   */
  public void setRotation (float angleDegrees) { rotationAngle = angleDegrees; }  // rotate sensor array about center
                                                                          
}