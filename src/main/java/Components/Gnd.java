package Components;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;


public class Gnd implements Component{

    final String type = "gnd";
    Image img;
    int sizeWidth, sizeHeight, x, y, ID;
    JPanel parent;
    HashMap<Integer, Component> connectedComponent = new HashMap<>();
    Component toldToUpdate = null;

    public Gnd(JPanel parent, int ID, int x, int y, int sizeWidth, int sizeHeight) {
        this.parent = parent;
        this.ID = ID;
        this.x = x - sizeWidth / 2;
        this.y = y - sizeHeight / 2;
        this.sizeWidth = sizeWidth;
        this.sizeHeight = sizeHeight;

        BufferedImage imgb = null;
        try {
            String path = System.getProperty("user.dir");
            imgb = ImageIO.read(new File(path + "\\src\\main\\resources\\gnd.png"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        img = imgb.getScaledInstance(sizeWidth, sizeHeight, Image.SCALE_SMOOTH);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Point getPosition() {
        return new Point(x, y);
    }

    public int getSizeWidth() {
        return sizeWidth;
    }

    public int getSizeHeight() {
        return sizeHeight;
    }

    public int getIDComponent() {
        return ID;
    }

    @Override
    public void resetPinIfContain(Component ID) {

    }

    @Override
    public Boolean isGrounded() {
        return true;
    }

    /**
     * uselles - always true so you wont need it
     * @param state state
     * @param pin pin
     */
    @Override
    public void setGrounded(boolean state, int pin) {
    }

    /**
     * then set the other component to ground because this class is gnd so..
     *
     * @param anotherComponent to connect with
     * @param pin of this transistor to set to
     * @param state state of the component which connect to
     */
    @Override
    public void setConnection(Component anotherComponent, int pin, boolean state) {
        connectedComponent.put(anotherComponent.getIDComponent(), anotherComponent.returnObjName());
        anotherComponent.setGrounded(true,anotherComponent.getPinFromAnotherObj(this));
    }



    @Override
    public void removeConnection() {
        for(Component c : connectedComponent.values()){
            c.resetPinIfContain(this);
            c.setGrounded(false,c.getPinFromAnotherObj(this));
        }
    }

    @Override
    public Component returnObjName() {
        return this;
    }

    @Override
    public Point inputTarget(int x, int y) {
        return new Point (ID,1);
    }

    @Override
    public boolean getState(int pin) {
        return false;
    }

    @Override
    public void setState(int pin, boolean state) {

    }

    @Override
    public int getPinFromAnotherObj(Component ObgID) {
        return 0;
    }

    @Override
    public void tellToUpdate(Component fromThisComponent) {
        this.toldToUpdate = fromThisComponent;
    }

    @Override
    public void update() {

    }

    @Override
    public String getType() {
        return type;
    }

    public void paint(Graphics g) {
        g.drawImage(img, x, y, parent);
    }
}
