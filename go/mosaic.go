package main

import (
	"fmt"
	"image"
	"image/draw"
	"image/jpeg"
	"log"
	"math"
	"os"
	"path"
)

const (
	SCALED_TILES_DIRECTORY = "scaledTiles"
)

type MosaicParameters struct {
	debug            bool
	tileBounds       image.Point
	sourceTileBounds image.Point
}

type tile struct {
	image *image.RGBA
	x     int
	y     int
}

func createMosaic(img image.Image, sourceTiles []image.Image, params MosaicParameters) image.Image {
	tiles := scaleTiles(sourceTiles, params.tileBounds)
	if params.debug {
		saveTiles(tiles)
	}

	originalBounds := img.Bounds()
	var gridDimensions image.Point
	gridDimensions.X = int(math.Ceil(float64(originalBounds.Max.X) / float64(params.sourceTileBounds.X)))
	gridDimensions.Y = int(math.Ceil(float64(originalBounds.Max.Y) / float64(params.sourceTileBounds.Y)))
	scaledImage := rescaleImage(img, gridDimensions.X*params.sourceTileBounds.X, gridDimensions.Y*params.sourceTileBounds.Y)

	tileChannel := make(chan tile, 10)

	go func(tileChannel chan<- tile) {
		for x := 0; x < gridDimensions.X; x++ {
			for y := 0; y < gridDimensions.Y; y++ {
				subRect := image.Rect(
					x*params.sourceTileBounds.X,
					y*params.sourceTileBounds.Y,
					(x+1)*params.sourceTileBounds.X,
					(y+1)*params.sourceTileBounds.Y)

				var t tile
				t.image = findClosestTile(scaledImage, subRect, tiles, params.tileBounds)
				t.x = x
				t.y = y
				tileChannel <- t
			}
		}
		close(tileChannel)
	}(tileChannel)

	mosaic := image.NewRGBA(image.Rect(0, 0, gridDimensions.X*params.tileBounds.X, gridDimensions.Y*params.tileBounds.Y))
	i := 0
	maxI := gridDimensions.X * gridDimensions.Y
	for tile := range tileChannel {
		draw.Draw(
			mosaic,
			image.Rect(
				tile.x*params.tileBounds.X,
				tile.y*params.tileBounds.Y,
				(tile.x+1)*params.tileBounds.X,
				(tile.y+1)*params.tileBounds.Y),
			tile.image,
			image.Pt(0, 0),
			draw.Src,
		)
		i++
		if i%1000 == 0 || i == maxI {
			log.Default().Printf("%d / %d = %f\n", i, maxI, float64(i)/float64(maxI))
		}
	}

	return mosaic
}

func scaleTiles(sourceTiles []image.Image, bounds image.Point) []*image.RGBA {
	var tiles []*image.RGBA
	tiles = make([]*image.RGBA, len(sourceTiles))

	for i, sourceTile := range sourceTiles {
		tiles[i] = rescaleImage(sourceTile, bounds.X, bounds.Y)
	}

	return tiles
}

func saveTiles(tiles []*image.RGBA) {
	err := os.MkdirAll(SCALED_TILES_DIRECTORY, os.FileMode(0x666))
	if err != nil {
		log.Default().Printf("Failed to create scaled tiles directory: %s, error: %s\n", SCALED_TILES_DIRECTORY, err)
		return
	}

	for i, tile := range tiles {
		tileFilename := fmt.Sprint(i) + ".jpeg"
		tilePath := path.Join(SCALED_TILES_DIRECTORY, tileFilename)
		file, err := os.Create(tilePath)
		if err != nil {
			log.Default().Println("Failed to create file for tile:", err)
			continue
		}
		defer file.Close()

		err = jpeg.Encode(file, tile, nil)
		if err != nil {
			log.Default().Println("Tile could not be saved:", err)
			continue
		}

		log.Default().Printf("Tile saved to: %s\n", tilePath)
	}
}

func rescaleImage(originalImage image.Image, targetWidth int, targetHeight int) *image.RGBA {
	originalBounds := originalImage.Bounds()
	img := image.NewRGBA(image.Rect(0, 0, targetWidth, targetHeight))
	for x := 0; x < targetWidth; x++ {
		for y := 0; y < targetHeight; y++ {
			nearestX := nearestTargetNeighbor(x, targetWidth, originalBounds.Max.X)
			nearestY := nearestTargetNeighbor(y, targetHeight, originalBounds.Max.Y)
			img.Set(x, y, originalImage.At(nearestX, nearestY))
		}
	}
	return img
}

func findClosestTile(originalImage *image.RGBA, subRect image.Rectangle, tiles []*image.RGBA, tileSize image.Point) *image.RGBA {
	var minScore uint64
	minScore = math.MaxUint64
	tileIndex := -1

	totalPixels := uint64(tileSize.X * tileSize.Y)
	for i, tile := range tiles {
		var r, g, b uint64
		for x := 0; x < tileSize.X; x++ {
			for y := 0; y < tileSize.Y; y++ {
				imageX := subRect.Min.X + nearestNeighbor(x, subRect.Dx(), tileSize.X)
				imageY := subRect.Min.Y + nearestNeighbor(y, subRect.Dy(), tileSize.Y)

				iPixel := tile.PixOffset(x, y)
				tr, tg, tb := tile.Pix[iPixel], tile.Pix[iPixel+1], tile.Pix[iPixel+2]

				iPixel = originalImage.PixOffset(imageX, imageY)
				ir, ig, ib := originalImage.Pix[iPixel], originalImage.Pix[iPixel+1], originalImage.Pix[iPixel+2]

				dr := ir - tr
				dg := ig - tg
				db := ib - tb

				r += uint64(dr * dr)
				g += uint64(dg * dg)
				b += uint64(db * db)
			}
		}
		tileScore := uint64(math.Sqrt(float64(r/totalPixels + g/totalPixels + b/totalPixels)))
		if tileScore < minScore {
			minScore = tileScore
			tileIndex = i
		}
	}

	if tileIndex < 0 {
		log.Fatal("Could not find a tile to fill in for image.")
	}

	return tiles[tileIndex]
}

func nearestNeighbor(val int, targetScale int, sourceScale int) int {
	return int((float32(val) / float32(sourceScale)) * float32(targetScale))
}

func nearestTargetNeighbor(val int, targetScale int, sourceScale int) int {
	return int((float32(val) / float32(targetScale)) * float32(sourceScale))
}
