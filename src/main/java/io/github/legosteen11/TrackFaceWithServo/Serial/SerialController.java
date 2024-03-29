package io.github.legosteen11.TrackFaceWithServo.Serial;

import gnu.io.*;
import io.github.legosteen11.TrackFaceWithServo.Main;
import io.github.legosteen11.TrackFaceWithServo.Servo.ServoPositionUpdater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;


public class SerialController implements SerialPortEventListener {
    // private String serialPortName = "/dev/ttyUSB0"; // change USB0 to ACM0 for RPi
    
    private BufferedReader input;
    /** The output stream to the port */
    private OutputStream output;
    /** Milliseconds to block while waiting for port open */
    private static final int TIME_OUT = 2000;
    /** Default bits per second for COM port. */
    private static final int DATA_RATE = 9600;
    
    SerialPort serialPort;
    /** The port we're normally going to use. */
    private static final String PORT_NAMES[] = {
            "/dev/tty.usbserial-A9007UX1", // Mac OS X
            "/dev/ttyACM0", // Raspberry Pi
            "/dev/ttyUSB0", // Linux
            "COM3", // Windows
    };
    
    public SerialController(String serialPortName) {
        System.setProperty("gnu.io.rxtx.SerialPorts", serialPortName);
        
        CommPortIdentifier portIdentifier = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        
        // Find the correct port
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName :
                    PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portIdentifier = currPortId;
                    break;
                }
            }
        }
        
        if(portIdentifier == null) {
            System.out.println("Could not find COM port...");
            return;
        }
        
        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), TIME_OUT);
            
            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            
            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();
            
            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            
        } catch (PortInUseException e) {
            System.out.println("Port is already in use...");
            e.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
    }

    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }
    
    public OutputStream getOutput() throws IOException {
        return serialPort.getOutputStream();
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine=input.readLine();
                if(Main.isVerbose()) System.out.println("Read: " + inputLine);
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }
}