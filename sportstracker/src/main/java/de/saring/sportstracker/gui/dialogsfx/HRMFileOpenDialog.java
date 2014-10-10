package de.saring.sportstracker.gui.dialogsfx;

import de.saring.exerciseviewer.parser.ExerciseParserFactory;
import de.saring.exerciseviewer.parser.ExerciseParserInfo;
import de.saring.sportstracker.core.STOptions;
import de.saring.sportstracker.gui.STContext;
import javafx.stage.FileChooser;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File Open dialog for HRM file selection.
 *
 * @author Stefan Saring
 */
public class HRMFileOpenDialog {

    private final STContext context;

    /**
     * Standard c'tor.
     *
     * @param context the SportsTracker context
     */
    @Inject
    public HRMFileOpenDialog(STContext context) {
        this.context = context;
    }

    /**
     * Displays the HRM File Open dialog and returns the selected file or null
     * when the user has nothing selected.
     *
     * @param options the application options
     * @param initialFile the filename to be initially selected (optional)
     * @return the selected file or null when nothing selected
     */
    public File selectHRMFile(final STOptions options, final String initialFile) {

        // create file chooser
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(context.getResReader().getString("st.dlg.hrm_file_open.title"));
        addFileExtensionFilters(fileChooser);

        // do we need to select an initial file ?
        final File fInitialFile = initialFile == null ? null : new File(initialFile);
        if (fInitialFile != null && fInitialFile.exists() && fInitialFile.isFile()) {
            fileChooser.setInitialDirectory(fInitialFile.getParentFile());
            fileChooser.setInitialFileName(fInitialFile.getName());
        } else {
            // use previous exercise directory as initial directory when available
            File initialDirectory;
            String strPreviousExerciseDirectory = options.getPreviousExerciseDirectory();

            if (strPreviousExerciseDirectory != null) {
                initialDirectory = new File(strPreviousExerciseDirectory);
            } else {
                // on first selection: use the user home directory
                initialDirectory = new File(System.getProperty("user.home"));
            }

            if (initialDirectory.exists()) {
                fileChooser.setInitialDirectory(initialDirectory);
            }
        }

        // display file chooser
        final File selectedFile = fileChooser.showOpenDialog(context.getPrimaryStage());
        if (selectedFile == null) {
            // nothing selected
            return null;
        }

        // store selected directory and return the selected file
        options.setPreviousExerciseDirectory(
                selectedFile.getParentFile().getAbsolutePath());
        return selectedFile;
    }

    /**
     * Adds the file extension filters for all supported parsers.
     *
     * @param fileChooser file chooser to add to
     */
    private void addFileExtensionFilters(FileChooser fileChooser) {
        List<ExerciseParserInfo> parserInfos = ExerciseParserFactory.getExerciseParserInfos();
        List<String> lAllExtensions = new ArrayList<>();

        // append a file filter for all ExerciseViewer file extensions
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                context.getFxResources().getString("st.dlg.hrm_file_open.filter_all"), "*.*"));

        parserInfos.forEach(parserInfo -> {
            final String filterName = String.format(
                    context.getFxResources().getString("st.dlg.hrm_file_open.filter_specific"), parserInfo.getName());

            // extend all filename suffixes with prefix "*."
            final List<String> extendedSuffixes = Stream.of(parserInfo.getSuffixes())
                    .map(suffix -> "*." + suffix)
                    .collect(Collectors.toList());

            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, extendedSuffixes));

            // append the parsers suffixes to the list of all extensions
            lAllExtensions.addAll(extendedSuffixes);
        });
    }
}
