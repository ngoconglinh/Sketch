import typing

import cv2
import numpy as np

class PencilSketch:

    def __init__(
        self,
        blur_simga: int = 5,
        ksize: typing.Tuple[int, int] = (0, 0),
        sharpen_value: int = None,
        kernel: np.ndarray = None,
        ) -> None:

        self.blur_simga = blur_simga
        self.ksize = ksize
        self.sharpen_value = sharpen_value
        self.kernel = np.array([[0, -1, 0], [-1, sharpen_value,-1], [0, -1, 0]]) if kernel == None else kernel

    def dodge(self, front: np.ndarray, back: np.ndarray) -> np.ndarray:

        result = back*255.0 / (255.0-front)
        result[result>255] = 255
        result[back==255] = 255
        return result.astype('uint8')

    def sharpen(self, image: np.ndarray) -> np.ndarray:
        if self.sharpen_value is not None and isinstance(self.sharpen_value, int):
            inverted = 255 - image
            return 255 - cv2.filter2D(src=inverted, ddepth=-1, kernel=self.kernel)

        return image

    def __call__(self, frame: np.ndarray) -> np.ndarray:

        grayscale = np.array(np.dot(frame[..., :3], [0.299, 0.587, 0.114]), dtype=np.uint8)
        grayscale = np.stack((grayscale,) * 3, axis=-1) # convert 1 channel grayscale image to 3 channels grayscale

        inverted_img = 255 - grayscale

        blur_img = cv2.GaussianBlur(inverted_img, ksize=self.ksize, sigmaX=self.blur_simga)

        final_img = self.dodge(blur_img, grayscale)

        sharpened_image = self.sharpen(final_img)

        return sharpened_image


