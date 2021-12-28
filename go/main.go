package main

import (
	"flag"
	"fmt"
	"image"
	_ "image/gif"
	"image/jpeg"
	_ "image/jpeg"
	_ "image/png"
	"log"
	"os"
	"path/filepath"
)

const (
	IMAGES_INIT_SIZE = 0
	IMAGES_CAP_SIZE  = 256

	DEFAULT_TILE_WIDTH  = 128
	DEFAULT_TILE_HEIGHT = 128

	DEFAULT_SOURCE_TILE_WIDTH  = 5
	DEFAULT_SOURCE_TILE_HEIGHT = 5
)

type Parameters struct {
	debug                bool
	targetImageFilename  string
	sourceImageDirectory string
	mosaicParameters     MosaicParameters
}

func getParameters() (Parameters, error) {
	var params Parameters

	debugFlag := flag.Bool("debug", false, "shows verbose debug logs and prints intermediate results")
	sourceImageDirectory := flag.String("image-dir", "images", "directory with images to use for mosaic")
	tileWidth := flag.Int("tile-width", DEFAULT_TILE_WIDTH, "specify width of replacement tile")
	tileHeight := flag.Int("tile-height", DEFAULT_TILE_HEIGHT, "specify height of replacement tile")
	sourceTileWidth := flag.Int("source-tile-width", DEFAULT_SOURCE_TILE_WIDTH, "specify grid width to replace from source image")
	sourceTileHeight := flag.Int("source-tile-height", DEFAULT_SOURCE_TILE_HEIGHT, "specify grid height to replace from source image")

	flag.Parse()

	tail := flag.Args()
	if len(tail) < 1 {
		log.Fatal("Must provide target image to make into a mosaic")
	}

	params.debug = *debugFlag
	params.mosaicParameters.debug = *debugFlag
	params.sourceImageDirectory = *sourceImageDirectory
	params.mosaicParameters.tileBounds.X = *tileWidth
	params.mosaicParameters.tileBounds.Y = *tileHeight
	params.mosaicParameters.sourceTileBounds.X = *sourceTileWidth
	params.mosaicParameters.sourceTileBounds.Y = *sourceTileHeight

	params.targetImageFilename = tail[0]

	if *debugFlag {
		log.Println(params)
	}

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
		image, err := loadImage(path)
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

	fmt.Printf("Image size(%d, %d)\n", targetImage.Bounds().Dx(), targetImage.Bounds().Dy())
	fmt.Println("Source images loaded:", len(sourceImages))

	mosaic := createMosaic(targetImage, sourceImages, parameters.mosaicParameters)

	outFile, err := os.Create("mosaic.jpg")
	if err != nil {
		log.Fatal(err)
	}
	defer outFile.Close()

	err = jpeg.Encode(outFile, mosaic, nil)
	if err != nil {
		log.Fatal(err)
	}
}
