package com.psx.technology.debug.phod.views;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class OverlayImageDescriptor extends CompositeImageDescriptor {
	private ImageDescriptor imageDescriptor;
	private ImageDescriptor overlayImage;
	Point size;
	Point overlaySize;
	public enum Position{Top_Left, Top_Right, Bottom_Right, Bottom_Left};
	Position position=null;

	public OverlayImageDescriptor(ImageDescriptor imgDescriptor, ImageDescriptor overlayImage) {
		this(imgDescriptor,overlayImage,Position.Top_Right);
	}
	
	public OverlayImageDescriptor(ImageDescriptor imgDescriptor, ImageDescriptor overlayImage, Position pos) {
		setImageDescriptor(imgDescriptor);
		setOverlayImage(overlayImage);
		this.position=pos;
	}

	protected void drawCompositeImage(int arg0, int arg1) {
		drawImage(getImageDescriptor().getImageData(), 0, 0);
		ImageData overlayImageData = getOverlayImage().getImageData();
		
		int xValue;
		int yValue;
		switch(position){
		case Top_Left:
			xValue = 0;
			yValue = 0;
			break;
		case Top_Right:
			xValue = size.x - overlaySize.x;
			yValue = 0;
			break;
		case Bottom_Left:
			xValue = 0;
			yValue = size.y - overlaySize.y;
			break;
		case Bottom_Right:
			xValue = size.x - overlaySize.x;
			yValue = size.y - overlaySize.y;
			break;
		default:
			xValue = 0;
			yValue = 0;
		}
		
		
		;
		drawImage(overlayImageData, xValue, yValue);
	}

	protected Point getSize() {
		return size;
	}

	public void setImageDescriptor(ImageDescriptor imageDescriptor) {
		this.imageDescriptor = imageDescriptor;
		Rectangle bounds = imageDescriptor.createImage().getBounds();
		size = new Point(bounds.width, bounds.height);
	}

	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	public void setOverlayImage(ImageDescriptor overlayImage) {
		this.overlayImage = overlayImage;
		Rectangle bounds = overlayImage.createImage().getBounds();
		overlaySize = new Point(bounds.width, bounds.height);
	}

	public ImageDescriptor getOverlayImage() {
		return overlayImage;
	}

}
