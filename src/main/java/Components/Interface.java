package Components;

import Savings.ReadObjects;
import Savings.SaveAll;
import Savings.WriteObjects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Interface extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    @Serial
    private static final long serialVersionUID = 6733046562158552384L;
    private HashMap<Integer, Component> componentMap = new HashMap<>();
    private HashMap<Line, ArrayList<Integer>> lines = new HashMap<>();
    private ArrayList<Line> tempLines = new ArrayList<>();
    private WindowElements.Menu menu;
    private double zoomFactor = 1, prevZoomFactor = 1, xOffset, yOffset;
    private int IDComponent = 1, IdReturnedTemp, IdComponentMoved, startDraggingX, startDraggingY;
    //dont use pin = 1 - 4 - 5 -> are used into returnPosition
    boolean npnTransistorToSet = false, vccToSet = false, gndToSet = false, deleteIsSelected = false, debugIsSelected = false,
            switchIsSelected = false, selectIsSelected = false, setConnection = false, mouseDragged = false, zoom = false,
            linesAlreadyEliminated = false, bitDisplayIsSelected = false, pnpTransistorToSet = false, textIsSelected = false;
    private Point tempConnectionPointFirstCall = new Point();
    private boolean setGrid = true;
    private Point mark1 = new Point(-10, 0);
    private boolean updateOnlyMarkLocator = false;
    private int lastComp;
    private String pathProject = null;
//  store the first clicked component id and the second one


    public Interface() {
        setBackground(Color.LIGHT_GRAY);
        Dimension size = new Dimension(900, 700);
        setPreferredSize(size);
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    /**
     * used to evaluate if a coordinate is located between two point
     *
     * @param coordinateMouse     - insert X or Y coordinate from the Mouse
     * @param coordinateComponent - insert X or Y coordinate from the origin of the image to evaluate
     * @param size                - the width or the height to add to coordinateComponenet
     * @return true if between the coordinates, false otherwise
     */
    public boolean isBetween(int coordinateMouse, int coordinateComponent, int size) {
        return coordinateMouse >= coordinateComponent && coordinateMouse <= (coordinateComponent + size);
    }

    /**
     * setted in DisplayFrame into the constructor
     * it is needed to set the name of the project and to turn on and of the grid option
     *
     * @param menu interface
     */
    public void setMenu(WindowElements.Menu menu) {
        this.menu = menu;
    }

    /**
     * This function is used to say if the click of the mouse is over a component or not.
     * if so, it will return the IDComponent (the area evaluated is a square)
     *
     * @param e - the MouseEvent given
     * @return the IDComponent of the component clicked. if 0 no component is over the mouse
     */
    public int checkMouseOverComponent(MouseEvent e) {
        for (Components.Component c : componentMap.values()) {
            if (isBetween(e.getX(), c.getPosition().x, c.getSizeWidth()) && isBetween(e.getY(), c.getPosition().y, c.getSizeHeight())) {
                return c.getIDComponent();
            }
        }
        return 0;
    }

    public int checkMouseOverComponent(int x, int y) {
        for (Components.Component c : componentMap.values()) {
            if (isBetween(x, c.getPosition().x, c.getSizeWidth()) && isBetween(y, c.getPosition().y, c.getSizeHeight())) {
                return c.getIDComponent();
            }
        }
        return 0;
    }

    /**
     * will remove a connection between a component and the IDComponent to eliminate
     *
     * @param IDComponent to delete
     */
    private void deleteLine(int IDComponent) {
        Iterator<Map.Entry<Line, ArrayList<Integer>>> iter = lines.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Line, ArrayList<Integer>> entry = iter.next();
            Line key = entry.getKey();
            if (key.contain(IDComponent)) {
                iter.remove();
            }
        }
    }

    /**
     * @param IDAndPin point containing on the X the ID of the component and the pin on the Y
     */
    private void deleteLine(Point IDAndPin) {
        Iterator<Map.Entry<Line, ArrayList<Integer>>> iter = lines.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Line, ArrayList<Integer>> entry = iter.next();
            Line key = entry.getKey();
            if (key.contain(IDAndPin.x, IDAndPin.y)) {
                iter.remove();
            }
        }
    }

    private void deleteLine(int id1, int id2) {
        Iterator<Map.Entry<Line, ArrayList<Integer>>> iter = lines.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Line, ArrayList<Integer>> entry = iter.next();
            Line key = entry.getKey();
            if (key.containForID(id1, id2)) {
                iter.remove();
            }
        }
    }

    private Point setInGridPosition(int x, int y) {
        int temp = calculateGrid(x);
        if (temp < 5) {
            x += temp;
        } else {
            temp = 10 - temp;
            x -= temp;
        }
        temp = calculateGrid(y);
        if (temp < 5) {
            y += temp;
        } else {
            temp = 10 - temp;
            y -= temp;
        }
        return new Point(x, y);
    }

    /**
     * used in setInGridPosition to not repite the cod for x and y
     *
     * @param val x or y
     * @return the number to sottract or to give to make the number to be % 10 = true
     */
    private int calculateGrid(int val) {
        int temp = val;
        boolean excess = false;
        int count = 0;
        while (temp % 10 != 0) {
            count++;
            temp++;
            if (count >= 5 && !excess) {
                excess = true;
            }
        }
        return count;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        zoomFactor = 1;
        prevZoomFactor = 1; //reset the zoom
        int xPos;
        int yPos;
        if (npnTransistorToSet) {
            int width = 30;
            int height = 30;
            xPos = e.getX() - width / 2;
            yPos = e.getY() - height / 2;
            if (setGrid) {
                Point gPos;
                gPos = setInGridPosition(xPos, yPos);
                xPos = gPos.x;
                yPos = gPos.y;
            }
            componentMap.put(IDComponent, new Transistor(this, IDComponent, xPos, yPos, width, height));
            IDComponent++;
        } else if (pnpTransistorToSet) {
            int width = 30;
            int height = 30;
            xPos = e.getX() - width / 2;
            yPos = e.getY() - height / 2;
            if (setGrid) {
                Point gPos;
                gPos = setInGridPosition(xPos, yPos);
                xPos = gPos.x;
                yPos = gPos.y;
            }
            componentMap.put(IDComponent, new PnpTransistor(this, IDComponent, xPos, yPos, width, height));
            IDComponent++;
        } else if (vccToSet) {
            int width = 15;
            int height = 15;
            xPos = e.getX() - width / 2;
            yPos = e.getY() - height / 2;
            if (setGrid) {
                Point gPos;
                gPos = setInGridPosition(xPos, yPos);
                xPos = gPos.x;
                yPos = gPos.y;
            }
            componentMap.put(IDComponent, new Vcc(this, IDComponent, xPos, yPos, width, height));
            IDComponent++;
        } else if (gndToSet) {
            int width = 25;
            int height = 25;
            xPos = e.getX() - width / 2;
            yPos = e.getY() - height / 2;
            if (setGrid) {
                Point gPos;
                gPos = setInGridPosition(xPos, yPos);
                xPos = gPos.x;
                yPos = gPos.y;
            }
            componentMap.put(IDComponent, new Gnd(this, IDComponent, xPos, yPos, width, height));
            // gndToSet = false;
            IDComponent++;
        } else if (switchIsSelected) {
            int width = 22;
            int height = 28;
            xPos = e.getX() - width / 2;
            yPos = e.getY() - height / 2;
            if (setGrid) {
                Point gPos;
                gPos = setInGridPosition(xPos, yPos);
                xPos = gPos.x;
                yPos = gPos.y;
            }
            componentMap.put(IDComponent, new Switch(this, IDComponent, xPos, yPos, width, height));
            IDComponent++;
        } else if (bitDisplayIsSelected) {
            int width = 22;
            int height = 28 + 8;
            xPos = e.getX() - width / 2;
            yPos = e.getY() - height / 2;
            if (setGrid) {
                Point gPos;
                gPos = setInGridPosition(xPos, yPos);
                xPos = gPos.x;
                yPos = gPos.y;
            }
            componentMap.put(IDComponent, new BitDisplay(this, IDComponent, xPos, yPos, width, height));//8 is the other oval for the gnd
            IDComponent++;
        } else if (deleteIsSelected) { //se l'ID tornato ( ovvero l'oggetto da eliminare è Vcc ) allora è possibile settare il pin to false
            int IdReturned = checkMouseOverComponent(e);
            if (IdReturned != 0 && IdReturned <= IDComponent) {
                deleteLine(IdReturned);
                componentMap.get(IdReturned).removeConnection();
                componentMap.remove(IdReturned);
                if (componentMap.size() == 0) {
                    IDComponent = 1;
                }
            }
        } else if (textIsSelected) {
            componentMap.put(IDComponent, new Text(this, IDComponent, e.getX(), e.getY(), 30, 20));
            Component componentReturned = componentMap.get(IDComponent);
            if (componentReturned.isGrounded()) {
                componentMap.remove(IDComponent);
            } else {
                IDComponent++;
            }
        } else if (selectIsSelected) {
            int IdReturned = checkMouseOverComponent(e);
            if (e.getButton() == 3) {
                Component component = componentMap.get(IdReturned);
                if (component != null) {
                    Point IDAndPin = component.inputTarget(e.getX(), e.getY());
                    if (IDAndPin.y != 0) {
                        try{
                        component.removeConnectionFromPins(IDAndPin.y);
                        deleteLine(IDAndPin);
                    } catch (StackOverflowError error) {
                        //error.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Something went wrong; if nothing has changed retry or delete the object", "Problem disconnecting", JOptionPane.WARNING_MESSAGE);
                            System.out.println("to resolve - asking loop from transistor to transistor");
                        }
                    }
                }
            } else {
                if (IdReturned != 0) {
                    Component componentReturned = componentMap.get(IdReturned);
                    if (componentReturned.getType().equalsIgnoreCase("switch") && componentReturned.inputTarget(e.getX(), e.getY()).y == 0) {
                        try {
                            componentReturned.setState(Switch.pinToChangeState, !componentReturned.getState(0));
                        } catch (StackOverflowError error) {
                            //error.printStackTrace();
                            System.out.println("to resolve - asking loop from transistor to transistor");
                        }
                    } else if (componentReturned.getType().equals("text")) {
                        componentReturned.setState(0, false);
                    } else if (!setConnection) {
                        IdReturnedTemp = IdReturned;
                        lastComp = IdReturned;
                        if ((tempConnectionPointFirstCall = componentReturned.inputTarget(e.getX(), e.getY())).y != 0) {
                            setConnection(true); // for not draw the green dot on the same obj
                        }
                    } else {
                        if (IdReturnedTemp != IdReturned) {
                            Point tempConnectionPointSecondCall;
                            if ((tempConnectionPointSecondCall = componentReturned.inputTarget(e.getX(), e.getY())).y != 0) {
                                connect(tempConnectionPointFirstCall, tempConnectionPointSecondCall);
                            }
                        }

                        setConnection(false);
                    }
                } else {//se il secondo click non è un componente
                    if (setConnection) {
                        setConnection(false);
                    }
                }
            }
        }
        if (debugIsSelected) {
            int IdReturned = checkMouseOverComponent(e);
            if (IdReturned != 0) {
                System.out.println(componentMap.get(IdReturned).toString());
            }
        }
        repaint();
    }

    /**
     * given the two component it will get the position from the component and give the exact location where to positionate
     * the line based on the component pin
     * <p>
     * - STILL TO IMPROVE AND ELIMINATE THE DUPLICATE PART -
     * it is duplicate because this function will add the two coordinate into an array filled with 4 integer(1.x,1.y,2.x,2.y)
     * and then are insert into a class Line (which include the 4 coordinates), and putted into the HashMap (Lines)
     *
     * @param firstIDComponentPin  a point where .X is the component ID and .y is the pin that is going to be paired with
     * @param secondIDComponentPin same as firstIDComponentPin
     */
    private void updateLine(Point firstIDComponentPin, Point secondIDComponentPin) {
        //inserisco i dati per disegnare
        ArrayList<Integer> linesCooridnate = new ArrayList<>();
        Components.Component firstComponent = componentMap.get(firstIDComponentPin.x).returnObjName();
        Components.Component secondComponent = componentMap.get(secondIDComponentPin.x).returnObjName();
        Point positionTemp;

        /*FIRST COMPONENT*/
        if (firstComponent.getType().equalsIgnoreCase("transistor")) {
            positionTemp = returnPosition(firstIDComponentPin.y, firstComponent);
            linesCooridnate.add(positionTemp.x);
            linesCooridnate.add(positionTemp.y);
        } else if (firstComponent.getType().equalsIgnoreCase("vcc")) {
            positionTemp = returnPosition(Vcc.pinVcc, firstComponent);//pin 4 just because it identify vcc into this function
            linesCooridnate.add(positionTemp.x);
            linesCooridnate.add(positionTemp.y);
        } else if (firstComponent.getType().equalsIgnoreCase("gnd")) {
            positionTemp = returnPosition(Gnd.pinGnd, firstComponent);//pin 4 just because it identify vcc into this function
            linesCooridnate.add(positionTemp.x);
            linesCooridnate.add(positionTemp.y);
        } else if (firstComponent.getType().equalsIgnoreCase("switch")) {
            positionTemp = returnPosition(Switch.pinConnectionSwitch, firstComponent);//pin 4 just because it identify vcc into this function
            linesCooridnate.add(positionTemp.x);
            linesCooridnate.add(positionTemp.y);
        } else if (firstComponent.getType().equalsIgnoreCase("bitDisplay")) {
            positionTemp = returnPosition(firstIDComponentPin.y, firstComponent);
            linesCooridnate.add(positionTemp.x);
            linesCooridnate.add(positionTemp.y);
        }
        /*SECOND COMPONENT*/

        if (secondComponent.getType().equalsIgnoreCase("transistor")) {
            positionTemp = returnPosition(secondIDComponentPin.y, secondComponent);
            linesCooridnate.add(positionTemp.x);
            linesCooridnate.add(positionTemp.y);
        } else if (secondComponent.getType().equalsIgnoreCase("vcc")) {
            positionTemp = returnPosition(Vcc.pinVcc, secondComponent);
            linesCooridnate.add(positionTemp.x);
            linesCooridnate.add(positionTemp.y);
        } else if (secondComponent.getType().equalsIgnoreCase("gnd")) {
            positionTemp = returnPosition(Gnd.pinGnd, secondComponent);
            linesCooridnate.add(positionTemp.x);
            linesCooridnate.add(positionTemp.y);
        } else if (secondComponent.getType().equalsIgnoreCase("switch")) {
            positionTemp = returnPosition(Switch.pinConnectionSwitch, secondComponent);
            linesCooridnate.add(positionTemp.x);
            linesCooridnate.add(positionTemp.y);
        } else if (secondComponent.getType().equalsIgnoreCase("bitDisplay")) {
            positionTemp = returnPosition(secondIDComponentPin.y, secondComponent);
            linesCooridnate.add(positionTemp.x);
            linesCooridnate.add(positionTemp.y);
        }

        Line coordinates = new Line(firstIDComponentPin.x, firstIDComponentPin.y, secondIDComponentPin.x, secondIDComponentPin.y);
        lines.put(coordinates, linesCooridnate);
        //linesUpdated = true;
    }

    private void setConnection(boolean state) {
        this.setConnection = state;
        if (!state) {
            lastComp = 0;
        } else {
            updateOnlyMarkLocator = false;
        }
    }

    /**
     * used in ReleasedMouse()
     * it get the element which has moved and pass it to updateLine
     *
     * @param line is the line that has to be changed
     */
    private void updateLineAfterDragged(Line line) {
        updateLine(new Point(line.getId1(), line.getPin1()), new Point(line.getId2(), line.getPin2()));
    }

    /**
     * is needed to locate precisely the location where the pin are located
     *
     * @param pin pin 1 if Vcc; pin 4 if switch; pin (every number exeption for 1-PinA-Transistor.pinB-4-5-6-Transistor.pinC) if gnd
     * @param c   component to get info about
     */
    private Point returnPosition(int pin, Component c) {
        Point position = new Point(0, 0);
        if (pin == Transistor.pinA) {
            position.x = c.getPosition().x + c.getSizeWidth() - 5;
            position.y = c.getPosition().y + c.getSizeHeight() - 26;
            return position;
        } else if (pin == Transistor.pinB) {
            position.x = c.getPosition().x + c.getSizeWidth() - 26;
            position.y = c.getPosition().y + c.getSizeHeight() - 15;
        } else if (pin == Transistor.pinC) {
            position.x = c.getPosition().x + c.getSizeWidth() - 5;
            position.y = c.getPosition().y + c.getSizeHeight() - 5;
        } else if (pin == Switch.pinConnectionSwitch) {//switch
            position.x = c.getPosition().x + c.getSizeWidth() / 2;
            position.y = c.getPosition().y + c.getSizeHeight() - 4;
        } else if (pin == Vcc.pinVcc) {//vcc
            position.x = c.getPosition().x + c.getSizeWidth() / 2 + 1;
            position.y = c.getPosition().y + c.getSizeHeight() / 2 + 1;
        } else if (pin == Gnd.pinGnd) {//gnd
            position.x = c.getPosition().x + c.getSizeWidth() / 2 + 1;
            position.y = c.getPosition().y + 3;
        } else if (pin == BitDisplay.pinHigh) {//bitDisplay
            position.x = c.getPosition().x + c.getSizeWidth() / 2;
            position.y = c.getPosition().y + 4;
        } else if (pin == BitDisplay.pinLow) {
            position.x = c.getPosition().x + c.getSizeWidth() / 2;
            position.y = c.getPosition().y + c.getSizeHeight() - 4;
        }
        return position;
    }

    /**
     * given two component this function will update th component's states
     * and will also call updateLine(); to from a visual connection trough them
     *
     * @param firstIDComponentPin  the first component clicked
     * @param secondIDComponentPin the second component clicked
     */
    public void connect(Point firstIDComponentPin, Point secondIDComponentPin) {
        updateLine(firstIDComponentPin, secondIDComponentPin);
        //.x = ID ; .y = pin
        Components.Component firstComponent = componentMap.get(firstIDComponentPin.x).returnObjName();
        Components.Component secondComponent = componentMap.get(secondIDComponentPin.x).returnObjName();
        try {
            /* Aggiorno prima il transistor */
            boolean flagGoodConnections = false;
            boolean flagGoodConnections1 = false;
            flagGoodConnections1 = firstComponent.setConnection(secondComponent, firstIDComponentPin.y, secondIDComponentPin.y, secondComponent.getState(secondIDComponentPin.y));
            flagGoodConnections = secondComponent.setConnection(firstComponent, secondIDComponentPin.y, firstIDComponentPin.y, firstComponent.getState(firstIDComponentPin.y));
            if (!flagGoodConnections || !flagGoodConnections1) {
                deleteLine(firstIDComponentPin.x, secondIDComponentPin.x);
            } else {
                firstComponent.updateAfterConnection();
                secondComponent.updateAfterConnection();
            }

        } catch (StackOverflowError e) {
            deleteLine(firstIDComponentPin.x, secondIDComponentPin.x);
            JOptionPane.showMessageDialog(this, "Something went wrong; if it won't work try deleting the component", "Connection error", JOptionPane.WARNING_MESSAGE);
        }

    }

    public void saveObjects() {

        WriteObjects wo = new WriteObjects(this);
        wo.saveObjects(componentMap, lines, setGrid);
        menu.setBoxText(wo.getProjectName());
        pathProject = wo.getPathProject();

    }

    public void readObject() {
        ReadObjects ro = new ReadObjects(this);
        if (ro.readComponents() != null) {
            componentMap = ro.readComponents();
            IDComponent = getLastIntegerValue();
            if (ro.getLines() != null) {
                lines = ro.getLines();
                for (Component c : componentMap.values()) {
                    c.update();
                }
            }
            setGrid = ro.getSavedInGrid();
            if (!setGrid) {
                menu.setGridOn();
            }
            pathProject = ro.getPathProject();
            menu.setBoxText(ro.getProjectName());
        }
        ro = null;
        select();
        repaint();
    }

    /**
     * retrive the last key <Integer> from componentMaps
     * it is needed to assign to IDComponent the highest ID coming from the savings
     *
     * @return an <Integer>
     */
    private int getLastIntegerValue() {
        int size = componentMap.size();
        int indexToSum = 0;
        for (Map.Entry<Integer, Component> set : componentMap.entrySet()) {
            size--;
            if (size == 0) {
                indexToSum = set.getKey();
            }
        }
        return indexToSum + 1;
    }

    /**
     * if called this function will set the parameter transistorToSet to true
     * and then after clicking on the jPanel it will rapresent the image
     */
    public void addTransistorNpn() {
        resetAll();
        npnTransistorToSet = true;
    }

    public void addTransistorPnp() {
        resetAll();
        pnpTransistorToSet = true;
    }

    public void addVcc() {
        resetAll();
        vccToSet = true;
    }

    public void addGnd() {
        resetAll();
        gndToSet = true;
    }

    public void select() {
        resetAll();
        selectIsSelected = true;
    }

    public void delete() {
        resetAll();
        deleteIsSelected = true;
    }

    public void addSwitch() {
        resetAll();
        switchIsSelected = true;
    }

    /**
     * fast way to turn every "componentIsSelected" to false
     */
    public void resetAll() {
        gndToSet = false;
        vccToSet = false;
        deleteIsSelected = false;
        selectIsSelected = false;
        switchIsSelected = false;
        debugIsSelected = false;
        bitDisplayIsSelected = false;
        npnTransistorToSet = false;
        pnpTransistorToSet = false;
        textIsSelected = false;
        lastComp = 0;
    }


    /**
     * se il componente esiste -> mi salvo la Line che sto modificando cosi da poterla ricreare dopo che il mouse si ferma
     * in caso sto muovendo l' intera schermata allora mi salvo tutto l'hashmap delle line e nel frattempo le elimino per evitare bug grafici
     * <p>
     * alla fine creo nuovamente partendo appunto dalle linee vecchie
     * quando clicco su un componente e inizio a muoverlo mouseDragged fa si che se il componente passa sopra un altro componente non
     * viene switchato, viene ripristinato a false solo quando rilascio il click del mouse
     * <p>
     * una volta finito di muoverlo ricollego tutto tramite la funzione updateLineAfterDragged() in mouseReleased()
     *
     * @param e mouseEvent
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (!mouseDragged) {
            mark1.x = -10;
            mouseDragged = true;
            IdComponentMoved = checkMouseOverComponent(e);
            startDraggingY = e.getY();
            startDraggingX = e.getX();
            Iterator<Map.Entry<Line, ArrayList<Integer>>> iter = lines.entrySet().iterator();
            while (iter.hasNext()) { //for single line or single component moving
                Map.Entry<Line, ArrayList<Integer>> mapEntry = (Map.Entry<Line, ArrayList<Integer>>) iter.next();
                Line key = mapEntry.getKey();
                if (mapEntry.getKey().contain(IdComponentMoved)) {
                    tempLines.add(key);
                    iter.remove();
                }
            }
        }
        if (IdComponentMoved != 0 && IdComponentMoved < IDComponent) {
            Component c = componentMap.get(IdComponentMoved);
            c.setPosition(new Point(e.getX() - c.getSizeWidth() / 2, e.getY() - c.getSizeHeight() / 2));
        } else {
            if (!linesAlreadyEliminated) {
                tempLines.addAll(lines.keySet());
                lines.clear();
                linesAlreadyEliminated = true;
            }
            for (Component c : componentMap.values()) {
                c.setPosition(new Point(c.getPosition().x + (e.getX() - startDraggingX), c.getPosition().y + (e.getY() - startDraggingY)));
            }
            startDraggingY = e.getY();
            startDraggingX = e.getX();
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (selectIsSelected) {
            int x = e.getX();
            int y = e.getY();
            int returnedComp = checkMouseOverComponent(x, y);
            if (returnedComp != 0) {
                Component comp = componentMap.get(returnedComp);
                int pin = comp.inputTarget(x, y).y;
                if (pin != 0 && pin != 6 && returnedComp != lastComp) { //6 è il pin che ritorna il text che non voglio disegnare
                    Point markLocation = returnPosition(pin, comp);
                    mark1.x = markLocation.x - 5;
                    mark1.y = markLocation.y - 5;
                    updateOnlyMarkLocator = true;
                    repaint();
                } else if (updateOnlyMarkLocator) {
                    removeDot();
                    updateOnlyMarkLocator = false;
                }
            } else if (updateOnlyMarkLocator) {
                removeDot();
                updateOnlyMarkLocator = false;
            }
        }

    }

    private void removeDot() {
        mark1.x = -10;
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    /**
     * usato principalmente per mouseDragged -> quando rilascio -> ripristino mouseDraged to false
     * aggiorno tutte le linee dopo aver aggiornato tutte le posizioni da mouseDragged();
     * <p>
     * in case setGrid is true -> allineo tutto in griglia
     *
     * @param e event from the mouse
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (mouseDragged && IdComponentMoved > 0 && IdComponentMoved < IDComponent) {
            if (setGrid) {
                Component temp = componentMap.get(IdComponentMoved);
                temp.setPosition(setInGridPosition(temp.getPosition().x, temp.getPosition().y));
            }
            callUpdateLineAfterDragged();
            repaint();
        }
        mouseDragged = false;
        if (linesAlreadyEliminated) {
            for (Component c : componentMap.values()) {
                if (setGrid) {
                    c.setPosition(setInGridPosition(c.getPosition().x, c.getPosition().y));
                }
            }
            callUpdateLineAfterDragged();
            repaint();
            linesAlreadyEliminated = false;
        }
    }

    private void callUpdateLineAfterDragged() {
        for (Line line : tempLines) {
            updateLineAfterDragged(line);
        }
        tempLines.clear();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void debug() {
        resetAll();
        debugIsSelected = true;
    }

    public void addBitDisplay() {
        resetAll();
        bitDisplayIsSelected = true;
    }

    public void addText() {
        resetAll();
        textIsSelected = true;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoom = true;
        resetAll();
        //Zoom in
        if (e.getWheelRotation() < 0) {
            zoomFactor *= 1.1;
            repaint();
        }
        //Zoom out
        if (e.getWheelRotation() > 0) {
            zoomFactor /= 1.1;
            repaint();
        }
    }

    public void clearPanel() {
        IDComponent = 1;
        lines.clear();
        componentMap.clear();
        repaint();
    }

    public boolean useGrid() {
        setGrid = !setGrid;
        return setGrid;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        if (zoom) {
            AffineTransform at = new AffineTransform();
            double xRel = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX();
            double yRel = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
            double zoomDiv = zoomFactor / prevZoomFactor;
            xOffset = (zoomDiv) * (xOffset) + (1 - zoomDiv) * xRel;
            yOffset = (zoomDiv) * (yOffset) + (1 - zoomDiv) * yRel;
            at.translate(xOffset, yOffset);
            at.scale(zoomFactor, zoomFactor);
            prevZoomFactor = zoomFactor;
            g2.transform(at);
            zoom = false;
        }

        g.setColor(Color.black);
        for (ArrayList<Integer> l : lines.values()) {
            g.drawLine(l.get(0), l.get(1), l.get(2), l.get(3));
        }
        for (Components.Component c : componentMap.values()) {
            c.paintComponent(g);
        }
        if (updateOnlyMarkLocator) {
            if (!setConnection) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.magenta);
            }
            if (mark1.x != -10) {
                //g.fillOval(mark1.x, mark1.y, 10, 10);
                g.drawOval(mark1.x, mark1.y, 10, 10);
            }
        }

    }

    public void saveOnSameProject() {
        if (pathProject != null) {
            SaveAll.saveObjectsWithPath(this, componentMap, lines, setGrid, pathProject);
        } else {
            saveObjects();
        }
    }
}
