package main;
/**
 * Data structure representing the type of a variable.
 * It contains a lexical unit, an image and the corresponding temporary ID in the LLVM code.
 *
 */
public class Type {
	LexicalUnit l;
	Image image = new Image();
	long LLVMTempId = 0;

	/**
	 * We update the image to know whether it will be truncated.
	 * Receiving a numerical value.
	 * @param str The number encountered.
	 */
	public void updateImage(String str) {

		if (str.charAt(0) == '+' || str.charAt(0) == '-'){	// Remove + or - if any.
			str = str.substring(1);
			this.image.signed = true;
		}
		if (str.contains(".")){
			String imageBefore = str.substring(0, str.indexOf('.'));
			this.image.digitBefore = (imageBefore.length() > this.image.digitBefore) ? imageBefore.length() : this.image.digitBefore;
			String imageAfter = str.substring(str.indexOf('.') + 1);
			this.image.digitAfter = (imageAfter.length() > this.image.digitAfter) ? imageAfter.length() : this.image.digitAfter;
		}
		else{
			this.image.digitBefore = (str.length() > this.image.digitBefore) ? str.length() : this.image.digitBefore;

		}

	}

	/**
	 * We update the image to know whether it will be truncated.
	 * Receiving an identifier.
	 * @param image
	 */
	public void updateImageWithImage(String image){
		if (image.charAt(0) == 's')
			this.image.signed = true;


		if (image.contains("v")){
			String imageBefore = image.substring(0, image.indexOf('v'));

			int digitBefore = (imageBefore.contains("(")) ? Integer.parseInt(imageBefore.substring(imageBefore.indexOf('(') + 1, imageBefore.indexOf(')'))) : 1;

			if (this.image.digitBefore < digitBefore)
				this.image.digitBefore = digitBefore;

			String imageAfter = image.substring(image.indexOf('v') + 1);

			int digitAfter = (imageAfter.contains("(")) ? Integer.parseInt(imageAfter.substring(imageAfter.indexOf('(') + 1, imageAfter.indexOf(')'))) : 1;

			if (this.image.digitAfter < digitAfter)
				this.image.digitAfter = digitAfter;

		}
		else{
			int digitBefore = (image.contains("(")) ? Integer.parseInt(image.substring(image.indexOf('(') + 1, image.indexOf(')'))) : 1;

			if (this.image.digitBefore < digitBefore)
				this.image.digitBefore = digitBefore;
		}
	}

}
