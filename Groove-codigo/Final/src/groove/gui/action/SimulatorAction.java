package groove.gui.action;

import groove.grammar.QualName;
import groove.grammar.model.GrammarModel;
import groove.grammar.model.ResourceKind;
import groove.grammar.type.TypeLabel;
import groove.gui.BehaviourOption;
import groove.gui.Icons;
import groove.gui.Options;
import groove.gui.Simulator;
import groove.gui.SimulatorModel;
import groove.gui.dialog.ErrorDialog;
import groove.gui.dialog.FindReplaceDialog;
import groove.gui.dialog.FreshNameDialog;
import groove.gui.dialog.SaveDialog;
import groove.gui.display.ControlDisplay;
import groove.gui.display.DisplayKind;
import groove.gui.display.DisplaysPanel;
import groove.gui.display.GroovyDisplay;
import groove.gui.display.LTSDisplay;
import groove.gui.display.PrologDisplay;
import groove.gui.display.ResourceDisplay;
import groove.gui.display.RuleDisplay;
import groove.gui.display.StateDisplay;
import groove.io.FileType;
import groove.io.GrooveFileChooser;
import groove.io.store.EditType;
import groove.io.store.SystemStore;
import groove.util.Duo;
import groove.util.Groove;
import groove.util.parse.FormatException;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Abstract action class for simulator actions.
 * The class contains a host of convenience methods for confirmation
 * dialogs.
 * The actual action to be taken on {@link #actionPerformed(ActionEvent)}
 * is delegated to an abstract method {@link #execute()}.
 */
public abstract class SimulatorAction extends AbstractAction implements
        Refreshable {
    /**
     * Internal constructor to set all fields.
     */
    protected SimulatorAction(Simulator simulator, String name, Icon icon,
            EditType edit, ResourceKind resource) {
        super(name, icon);
        this.simulator = simulator;
        this.resource = resource;
        this.edit = edit;
        putValue(SHORT_DESCRIPTION, name);
        setEnabled(false);
        if (simulator != null) {
            simulator.getActions().addRefreshable(this);
        }
    }

    /**
     * Creates an initially disabled action for a given simulator,
     * and with a given name and (possibly {@code null}) icon.
     * The action adds itself to the refreshables of the simulator.
     */
    protected SimulatorAction(Simulator simulator, String name, Icon icon) {
        this(simulator, name, icon, null, null);
    }

    /**
     * Creates an initially disabled edit action for a given simulator.
     * The edit type and edited resource automatically generate the name and icon.
     * The action adds itself to the refreshables of the simulator.
     */
    protected SimulatorAction(Simulator simulator, EditType edit,
            ResourceKind resource) {
        this(simulator, Options.getEditActionName(edit, resource, false),
            Icons.getEditIcon(edit, resource), edit, resource);
    }

    /** Returns the edit name for this action, if it is an edit action. */
    protected String getEditActionName() {
        if (getEditType() == null) {
            return null;
        } else {
            return Options.getEditActionName(getEditType(), getResourceKind(),
                false);
        }
    }

    /** The simulator on which this action works. */
    protected final Simulator getSimulator() {
        return this.simulator;
    }

    /** Convenience method to retrieve the simulator model. */
    protected final SimulatorModel getSimulatorModel() {
        return getSimulator().getModel();
    }

    /** Convenience method to retrieve the grammar model from the simulator model. */
    protected final GrammarModel getGrammarModel() {
        return getSimulatorModel().getGrammar();
    }

    /** Convenience method to retrieve the grammar store from the simulator model. */
    protected final SystemStore getGrammarStore() {
        return getSimulatorModel().getStore();
    }

    /** Convenience method to retrieve the simulator action store. */
    protected final ActionStore getActions() {
        return getSimulator().getActions();
    }

    /** Convenience method to retrieve the frame of the simulator. */
    protected final JFrame getFrame() {
        return getSimulator().getFrame();
    }

    /** Convenience method to retrieve the main simulator panel. */
    protected final DisplaysPanel getDisplaysPanel() {
        return getSimulator().getDisplaysPanel();
    }

    /** 
     * Returns the simulator display for the resource kind of this action.
     * @throws IllegalStateException if there is no resource kind
     */
    protected final ResourceDisplay getDisplay() {
        if (getResourceKind() == null) {
            throw new IllegalStateException();
        }
        switch (getResourceKind()) {
        case CONTROL:
            return getControlDisplay();
        case HOST:
            return getHostDisplay();
        case PROLOG:
            return getPrologDisplay();
        case RULE:
            return getRuleDisplay();
        case TYPE:
            return getTypeDisplay();
        case GROOVY:
            return getGroovyDisplay();
        case PROPERTIES:
        default:
            assert false;
            return null;
        }
    }

    /** Convenience method to retrieve the state panel of the simulator. */
    protected final ResourceDisplay getHostDisplay() {
        return (ResourceDisplay) getDisplaysPanel().getDisplay(DisplayKind.HOST);
    }

    /** Convenience method to retrieve the rule panel of the simulator */
    protected final RuleDisplay getRuleDisplay() {
        return (RuleDisplay) getDisplaysPanel().getDisplay(DisplayKind.RULE);
    }

    /** Convenience method to retrieve the type panel of the simulator. */
    protected final ResourceDisplay getTypeDisplay() {
        return (ResourceDisplay) getDisplaysPanel().getDisplay(DisplayKind.TYPE);
    }

    /** Returns the control panel that owns the action. */
    protected final ControlDisplay getControlDisplay() {
        return (ControlDisplay) getDisplaysPanel().getDisplay(
            (DisplayKind.CONTROL));
    }

    /** Returns the prolog panel that owns the action. */
    protected final PrologDisplay getPrologDisplay() {
        return (PrologDisplay) getDisplaysPanel().getDisplay(DisplayKind.PROLOG);
    }

    /** Returns the groovy panel that owns the action. */
    protected final GroovyDisplay getGroovyDisplay() {
        return (GroovyDisplay) getDisplaysPanel().getDisplay(DisplayKind.GROOVY);
    }

    /** Returns the LTS panel that owns the action. */
    protected final LTSDisplay getLtsDisplay() {
        return (LTSDisplay) getDisplaysPanel().getDisplay(DisplayKind.LTS);
    }

    /** Returns the state panel that owns the action. */
    protected final StateDisplay getStateDisplay() {
        return (StateDisplay) getDisplaysPanel().getDisplay(DisplayKind.STATE);
    }

    /** Returns the (possibly {@code null}) edit type of this action.*/
    protected final EditType getEditType() {
        return this.edit;
    }

    /** Returns the (possibly {@code null}) grammar resource being edited by this action.*/
    protected final ResourceKind getResourceKind() {
        return this.resource;
    }

    /** Disposes the action by unregistering it as a listener. */
    public void dispose() {
        getActions().removeRefreshable(this);
    }

    @Override
    public void refresh() {
        setEnabled(true);
    }

    /** Delegates to {@link #execute()}. */
    @Override
    public void actionPerformed(ActionEvent e) {
        execute();
    }

    /**
     * Method to execute the action encapsulated by this class.
     * Called from {@link #actionPerformed(ActionEvent)}.
     */
    public abstract void execute();

    /**
     * Enters a dialog that results in a name that is not in a set of
     * current names, or <code>null</code> if the dialog was cancelled.
     * @param name an initially proposed name
     * @param mustBeFresh if <code>true</code>, the returned name is guaranteed
     *        to be distinct from the existing names
     * @return a type graph not occurring in the current grammar, or
     *         <code>null</code>
     */
    protected final String askNewName(String name, boolean mustBeFresh) {
        ResourceKind kind = getResourceKind();
        String title =
            String.format("Select %s%s name", mustBeFresh ? "new " : "",
                kind.getDescription());
        Set<String> existingNames =
            getSimulatorModel().getGrammar().getNames(kind);
        FreshNameDialog<String> nameDialog =
            new FreshNameDialog<String>(existingNames, name, mustBeFresh) {
                @Override
                protected String createName(String name) {
                    return name;
                }
            };
        nameDialog.showDialog(getFrame(), title);
        return nameDialog.getName();
    }

    /**
     * Invokes a file chooser of the right type to save a given aspect graph,
     * and returns the chosen (possibly {@code null}) file.
     */
    protected final File askSaveResource(String name) {
        FileType filter = getResourceKind().getFileType();
        GrooveFileChooser chooser = GrooveFileChooser.getInstance(filter);
        chooser.setSelectedFile(new File(name));
        return SaveDialog.show(chooser, getFrame(), null);
    }

    /**
     * Enters a dialog that asks for a label to be renamed, and its the
     * replacement.
     * @return A pair consisting of the label to be replaced and its
     *         replacement, neither of which can be <code>null</code>; or
     *         <code>null</code> if the dialog was cancelled.
     */
    protected final Duo<TypeLabel> askFindSearch(TypeLabel oldLabel) {
        FindReplaceDialog dialog =
            new FindReplaceDialog(
                getSimulatorModel().getGrammar().getTypeGraph(), oldLabel);
        int dialogResult = dialog.showDialog(getFrame(), null);
        Duo<TypeLabel> result;
        switch (dialogResult) {
        case FindReplaceDialog.FIND:
            result = new Duo<TypeLabel>(dialog.getOldLabel(), null);
            break;
        case FindReplaceDialog.REPLACE:
            result =
                new Duo<TypeLabel>(dialog.getOldLabel(), dialog.getNewLabel());
            break;
        case FindReplaceDialog.CANCEL:
        default:
            result = null;
        }
        return result;
    }

    /**
     * Creates and shows an {@link ErrorDialog} for a given message and
     * exception.
     */
    protected final void showErrorDialog(Throwable exc, String message,
            Object... args) {
        final ErrorDialog dialog =
            new ErrorDialog(getFrame(), String.format(message, args), exc);
        if (SwingUtilities.isEventDispatchThread()) {
            dialog.setVisible(true);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setVisible(true);
                    }
                });
            } catch (InterruptedException e) {
                // do nothing
            } catch (InvocationTargetException e) {
                // do nothing
            }
        }
    }

    /**
     * Checks if a given option is confirmed.
     */
    protected final boolean confirmBehaviourOption(String option) {
        return confirmBehaviour(option, null);
    }

    /**
     * Checks if a given option is confirmed. The question can be set
     * explicitly.
     */
    protected final boolean confirmBehaviour(String option, String question) {
        BehaviourOption menu =
            (BehaviourOption) getSimulator().getOptions().getItem(option);
        return menu.confirm(getFrame(), question);
    }

    /**
     * Asks whether a given existing resource, of a the kind of this action,
     * should be replaced by a newly loaded one.
     */
    protected final boolean confirmOverwrite(String name) {
        return confirmOverwrite(getResourceKind(), name);
    }

    /**
     * Asks whether a given existing resource, of a given kind,
     * should be replaced by a newly loaded one.
     */
    protected final boolean confirmOverwrite(ResourceKind resource, String name) {
        int response =
            JOptionPane.showConfirmDialog(
                getFrame(),
                String.format("Replace existing %s '%s'?",
                    resource.getDescription(), name), null,
                JOptionPane.OK_CANCEL_OPTION);
        return response == JOptionPane.OK_OPTION;
    }

    /**
     * Asks whether a given existing file should be overwritten by a new
     * grammar.
     */
    protected final boolean confirmOverwriteGrammar(File grammarFile) {
        if (grammarFile.exists()) {
            int response =
                JOptionPane.showConfirmDialog(getFrame(),
                    "Overwrite existing grammar?", null,
                    JOptionPane.OK_CANCEL_OPTION);
            return response == JOptionPane.OK_OPTION;
        } else {
            return true;
        }
    }

    /**
     * Returns the file chooser for rule (GPR) files, lazily creating it first.
     */
    protected final JFileChooser getRuleFileChooser() {
        return GrooveFileChooser.getInstance(FileType.RULE);
    }

    /**
     * Returns the file chooser for state (GST or GXL) files, lazily creating it
     * first.
     */
    protected final JFileChooser getStateFileChooser() {
        return GrooveFileChooser.getInstance(FileType.HOSTS);
    }

    /**
     * Return a file chooser for prolog files
     */
    protected final JFileChooser getPrologFileChooser() {
        return GrooveFileChooser.getInstance(FileType.PROLOG);
    }

    /**
     * Returns the file chooser for grammar (GPS) files, lazily creating it
     * first.
     */
    protected final JFileChooser getGrammarFileChooser() {
        return getGrammarFileChooser(false);
    }

    /**
     * Returns the file chooser for grammar (GPS) files, lazily creating it
     * first.
     * @param includeArchives flag to indicate if archive (ZIP and JAR) files
     * should also be recognised by the chooser
     */
    protected final JFileChooser getGrammarFileChooser(boolean includeArchives) {
        if (includeArchives) {
            return GrooveFileChooser.getInstance(FileType.GRAMMARS);
        } else {
            return GrooveFileChooser.getInstance(FileType.GRAMMAR);
        }
    }

    /**
     * Returns the last file from which a grammar was loaded.
     */
    protected final File getLastGrammarFile() {
        File result = null;
        SystemStore store = getSimulatorModel().getStore();
        Object location = store == null ? null : store.getLocation();
        if (location instanceof File) {
            result = (File) location;
        } else if (location instanceof URL) {
            result = Groove.toFile((URL) location);
        }
        return result;
    }

    /**
     * Constructs a grammar element name from a file, if it is within the grammar location.
     * The element name is relative to the grammar location.
     * @param selectedFile file for the grammar element, possibly with extension
     * @throws IOException if the name is not well-formed
     */
    protected final String getNameInGrammar(File selectedFile)
        throws IOException {
        FileType filter = getResourceKind().getFileType();
        // find out if this is within the grammar directory
        String selectedPath =
            filter.stripExtension(selectedFile.getCanonicalPath());
        String name = null;
        Object location = getSimulatorModel().getStore().getLocation();
        if (location instanceof File) {
            String grammarPath = ((File) location).getCanonicalPath();
            if (selectedPath.startsWith(grammarPath)) {
                String diff = selectedPath.substring(grammarPath.length());
                File pathDiff = new File(diff);
                List<String> pathFragments = new LinkedList<String>();
                while (pathDiff.getName().length() > 0) {
                    pathFragments.add(0, pathDiff.getName());
                    pathDiff = pathDiff.getParentFile();
                }
                try {
                    QualName qualName = new QualName(pathFragments);
                    qualName.testValid();
                    name = qualName.toString();
                } catch (FormatException e) {
                    throw new IOException(String.format(
                        "Malformed %s name: %s",
                        getResourceKind().getDescription(), e.getMessage()));
                }
            }
        }
        return name;
    }

    /** The simulator on which this action works. */
    private final Simulator simulator;
    /** Possibly {@code null} edit type of this action. */
    private final EditType edit;
    /** Possibly {@code null} resource being edited by this action. */
    private final ResourceKind resource;
}
