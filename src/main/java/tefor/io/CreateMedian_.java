package tefor.io;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.plugin.ZProjector;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;
import fiji.util.gui.GenericDialogPlus;

/**
 * Plugin to create the median image out of a set of images. 
 */
public class CreateMedian_ implements PlugIn {
	
	/** source directory **/
	public static String sourceDirectory = "";
	/** flat to use virtual stacks */
	public static boolean useVirtual = true;
	
	@Override
	public void run(String arg) {
		GenericDialogPlus gd = new GenericDialogPlus("Create Median Volume");

		gd.addDirectoryField("Source directory", sourceDirectory, 50);
		gd.addCheckbox("Open files as virtual stacks", useVirtual );
		
		gd.showDialog();
		
		// Exit when canceled
		if (gd.wasCanceled()) 
			return;
				
		sourceDirectory = gd.getNextString();
		useVirtual = gd.getNextBoolean();
		
		String source_dir = sourceDirectory;
		if (null == source_dir) 
		{
			IJ.error("Error: No source directory was provided.");
			return;
		}
		
		// Check if source directory exists
		if( (new File( source_dir )).exists() == false )
		{
			IJ.error("Error: source directory " + source_dir + " does not exist.");
			return;
		}
		
		source_dir = source_dir.replace('\\', '/');
		if (!source_dir.endsWith("/")) source_dir += "/";
		
		ImagePlus res = exec( source_dir, useVirtual );
		if( null != res )
			res.show();
	}
	/**
	 * Backend method to calculate median image out of a list of image files.
	 * @param source_dir path to the image files
	 * @param virtual flag to use virtual stacks
	 * @return median image
	 */
	public static ImagePlus exec( String source_dir, boolean virtual )
	{
		// get file listing		
		final String[] names = new File(source_dir).list(); 
		Arrays.sort(names);
		ImagePlus[] images = new ImagePlus[ names.length ];
		long startTime = System.currentTimeMillis();
		for( int i = 0; i < names.length; i++ )
		{
			ImporterOptions options = null;
			try {
				options = new ImporterOptions();
				// open as virtual stack if set by user
				options.setVirtual( virtual );
				options.setId( source_dir + "/" + names[ i ] );
				IJ.log( "Opening image " + names[ i ] + "..." );
				ImagePlus[] img = BF.openImagePlus(options);
				images[ i ] = img[ 0 ];
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}catch ( FormatException e ) {
				e.printStackTrace();
				return null;
			}
			
		}
		long endTime = System.currentTimeMillis();
		IJ.log( "Opening images took " +( endTime - startTime ) + " milliseconds." );
		
		final int width = images[0].getWidth();
		final int height = images[0].getHeight();
		final int nSlices = images[0].getNSlices();
		
		
		ImageStack resStack = new ImageStack( width, height );
		IJ.log( "Calculating median volume..." );
		for( int n = 1; n <= nSlices; n++ )
		{
			IJ.showStatus( "Calculating median of slice " + n + "/" + nSlices );
			IJ.showProgress( n , nSlices );
			ImageStack projStack = new ImageStack(width, height);
			for( int i = 0; i < names.length; i ++ )
			{
				projStack.addSlice("", images[ i ].getImageStack().getProcessor( n ) );
			}
			ImagePlus projectStack = new ImagePlus("membraneStack", projStack);
			ZProjector zp = new ZProjector( projectStack );
			zp.setMethod( ZProjector.MEDIAN_METHOD );
			zp.doProjection();
			resStack.addSlice( "Median-slice-"+ n, zp.getProjection().getChannelProcessor() );
		}
		endTime = System.currentTimeMillis();
		IJ.log( "Whole plugin took " +( endTime - startTime ) + " milliseconds." );
		ImagePlus result = new ImagePlus( "Median volume", resStack );
		result.setCalibration( images[ 0 ].getCalibration() );
		return result;
	}
	
}
