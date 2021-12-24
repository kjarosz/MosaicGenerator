package main

import (
	"flag"
	"fmt"
	"image"
	_ "image/jpeg"
	"log"
	"os"
	"path/filepath"
)

const (
	IMAGES_INIT_SIZE = 64
	IMAGES_CAP_SIZE  = 256
)

type Parameters struct {
	targetImageFilename  string
	sourceImageDirectory string
}

func getParameters() (Parameters, error) {
	var params Parameters

	sourceImageDirectory := flag.String("image-dir", "images", "directory with images to use for mosaic")

	flag.Parse()

	params.sourceImageDirectory = *sourceImageDirectory

	tail := flag.Args()
	if len(tail) < 1 {
		log.Fatal("Must provide target image to make into a mosaic")
	}

	params.targetImageFilename = tail[0]

	return params, nil
}

func loadImage(filename string) (image.Image, error) {
	file, err := os.Open(filename)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	image, format, err := image.Decode(file)
	if err != nil {
		return nil, err
	}

	fmt.Println("Loaded image", filename, "-", format)

	return image, nil
}

func loadSourceImages(sourceDirectory string) ([]image.Image, error) {
	var images []image.Image
	images = make([]image.Image, IMAGES_INIT_SIZE, IMAGES_CAP_SIZE)
	err := filepath.WalkDir(sourceDirectory, func(path string, dir os.DirEntry, err error) error {
		image, err = loadImage(path)
		if err == nil {
			images = append(images, image)
		}
		return nil
	})
	if err != nil {
		log.Fatal(err)
	}
	return images, nil
}

func main() {
	parameters, err := getParameters()
	if err != nil {
		log.Fatal(err)
	}

	targetImage, err := loadImage(parameters.targetImageFilename)
	if err != nil {
		log.Fatal(err)
	}

	sourceImages, err := loadSourceImages(parameters.sourceImageDirectory)
	if err != nil {
		log.Fatal(err)
	}

	fmt.Printf("Image size(%d, %d)", targetImage.Bounds().Dx(), targetImage.Bounds().Dy())
	fmt.Printf("Source images loaded:", len(sourceImages))
}
