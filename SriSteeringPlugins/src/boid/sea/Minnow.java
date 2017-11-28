/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boid.sea;

import buckland.ch3.boid.Prey;
import buckland.ch3.common.D2.Vector2D;
import static buckland.ch3.common.misc.Cgdi.gdi;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author TSM
 */
public class Minnow extends Prey {
    
    //Refactors the antenna length using speed
    public final double SPEED_REFACTOR = 0.20;
    
    /**
     * Constructor
     * @param spawnPos 
     */
    public Minnow(Vector2D spawnPos) {
        super(spawnPos);
    }
    
    /**
     * This method is invoked during the render phase of the game world
     * @param bTrueFalse 
     */
    @Override
    public void Render(boolean bTrueFalse) {
        
        //Render the background objects for the prey
        super.Render(bTrueFalse);
        drawMinnowAntenna();
    }

    /**
     * This method draws the antenna for the prey
     */
    public void drawMinnowAntenna() {
        Vector2D pHeading = this.Heading();
        double dLength = SPEED_REFACTOR * this.m_vVelocity.Length();
        double dTheta = Math.atan2(pHeading.y, pHeading.x);
        
        // This is where the antenna starts which is known by prey position
        int dXStart = (int) (this.Pos().x + 0.5);
        int dYStart = (int) (this.Pos().y + 0.5);
        
        // The end location of the antenna is knowable using algebra and trig:
        // cos(theta) = deltaX / L = (xEnd - xStart) / L
        // sin(theta) = deltaY / L = (yEnd - yStart) / L
        // So, we can solve for xEnd and yEnd
        int dXEnd = (int) (dLength * Math.cos(dTheta) + dXStart + 0.5);
        int dYEnd = (int) (dLength * Math.sin(dTheta) + dYStart + 0.5);
        
        //Get the graphics context
        Graphics2D g = gdi.GetHdc();
        //Sets color for the antenna
        g.setColor(Color.GREEN);
        //Draws the antenna on prey
        g.drawLine(dXStart,dYStart,dXEnd,dYEnd);

        
    }
    
}
