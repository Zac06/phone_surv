import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class MainMIDlet extends MIDlet implements CommandListener {
    private Display display;

    private static Form form;

    private Command exitCommand;
    private Command menuCommand;
    private Command backCommand;
    private Command startCommand;

    private List menuList;

    private TextField numberField;
    private TextField camNameField;

    private PictureTaker pt;

    public MainMIDlet() {
        display = Display.getDisplay(this);

        form = new Form("IP Camera");
        exitCommand = new Command("Exit", Command.EXIT, 0);
        menuCommand = new Command("Menu", Command.SCREEN, 1);
        backCommand = new Command("Back", Command.BACK, 0);
        startCommand = new Command("Start", Command.SCREEN, 2);

        form.addCommand(exitCommand);
        form.addCommand(menuCommand);
        form.addCommand(startCommand);

        // numeric input field (replaces the hardcoded "24")
        numberField = new TextField("Parameter:", "24", 3, TextField.NUMERIC);
        form.append(numberField);

        // NEW: camera name field for the extra parameter
        camNameField = new TextField("Camera name:", "DefaultCam", 30, TextField.ANY);
        form.append(camNameField);

        form.setCommandListener(this);

        menuList = new List("Menu", List.IMPLICIT);
        menuList.append("Option 1", null);
        menuList.append("Option 2", null);
        menuList.addCommand(backCommand);

        menuList.setCommandListener(this);
    }

    public void startApp() {
        display.setCurrent(form);
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}

    public void commandAction(Command c, Displayable d) {
        if (c == exitCommand) {
            if (pt != null) pt.stop();
            notifyDestroyed();
        } else if (c == menuCommand) {
            display.setCurrent(menuList);
        } else if (c == backCommand) {
            display.setCurrent(form);
        } else if (c == startCommand) {
            try {
                int param = Integer.parseInt(numberField.getString());
                String camName = camNameField.getString();

                // Constructor now includes the extra camera name
                pt = new PictureTaker("Pic-send-1", "192.168.1.2", 55555, param, camName);
                pt.start();
                log("Started PictureTaker param=" + param + " camName=" + camName);
            } catch (Exception e) {
                log("Error: " + e.toString());
            }
        } else if (c == List.SELECT_COMMAND) {
            int index = menuList.getSelectedIndex();
            Alert alert = new Alert("Selected Option", "You selected option " + (index + 1), null, AlertType.INFO);
            alert.setTimeout(2000);
            display.setCurrent(alert, menuList);
        }
    }

    public static void log(String s) {
        int count = form.size();
        if (count > 2) { // we always keep the first two fields
            form.delete(count - 1); // remove the last appended item
        }

        StringItem item = new StringItem(null, s + "\n");
        form.append(item);
    }
}
