package tefor.io;

import java.io.IOException;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import sc.fiji.io.Nrrd_Writer;
/**
 * Plugin to save images as compressed NRRD files.
 */
public class SaveAsGzipNrrd_ implements PlugIn {

	@Override
	public void run(String arg) {
		
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.showMessage( "Error: there are no images open to save" );
			return;
		}
		
		String name = arg;
		if (arg == null || arg.equals("")) {
			name = imp.getTitle();
		}
		
		SaveDialog sd = new SaveDialog("Save as compressed Nrrd ...", name, ".nrrd");
		String file = sd.getFileName();
		if (file == null) return;
		String directory = sd.getDirectory();

		Nrrd_Writer writer = new Nrrd_Writer();
		
		try {
			writer.setNrrdEncoding("gzip");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try{
			writer.save( imp, directory, file );
		} catch ( Exception ex ) {
			IJ.error("An error occured writing the file.\n \n" + ex );
			IJ.showStatus("");
		}
		
	}

}
