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

	mosaic := image.NewRGBA(image.Rect(0, 0, gridDimensions.X*params.tileBounds.X, gridDimensions.Y*params.tileBounds.Y))
	i := 0
	maxI := gridDimensions.X * gridDimensions.Y
	for x := 0; x < gridDimensions.X; x++ {
		for y := 0; y < gridDimensions.Y; y++ {
			subRect := image.Rect(
				x*params.sourceTileBounds.X,
				y*params.sourceTileBounds.Y,
				x*params.sourceTileBounds.X+params.sourceTileBounds.X,
				y*params.sourceTileBounds.Y+params.sourceTileBounds.Y)
			tile := findClosestTile(scaledImage, subRect, tiles, params.tileBounds)
			for mx := 0; mx < params.tileBounds.X; mx++ {
				for my := 0; my < params.tileBounds.Y; my++ {
					draw.Draw(
						mosaic,
						image.Rect(
							mx*params.tileBounds.X,
							my*params.tileBounds.Y,
							(mx+1)*params.tileBounds.X,
							(my+1)*params.tileBounds.Y),
						tile,
						image.Pt(0, 0),
						draw.Src,
					)
				}
			}
			i++
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
			nearestX := nearestNeighbor(x, targetWidth, originalBounds.Max.X)
			nearestY := nearestNeighbor(y, targetHeight, originalBounds.Max.Y)
			img.Set(x, y, originalImage.At(nearestX, nearestY))
		}
	}
	return img
}

func findClosestTile(originalImage *image.RGBA, subRect image.Rectangle, tiles []*image.RGBA, tileSize image.Point) *image.RGBA {
	var minScore uint64
	minScore = math.MaxUint64
	tileIndex := -1

	for i, tile := range tiles {
		var tileScore uint64
		for x := 0; x < tileSize.X; x++ {
			for y := 0; y < tileSize.Y; y++ {
				imageX := subRect.Min.X + nearestNeighbor(x, subRect.Dx(), tileSize.X)
				imageY := subRect.Min.Y + nearestNeighbor(y, subRect.Dy(), tileSize.Y)

				tr, tg, tb, _ := tile.RGBAAt(x, y).RGBA()
				ir, ig, ib, _ := originalImage.At(imageX, imageY).RGBA()

				dr := ir/257 - tr/257
				dg := ig/257 - tg/257
				db := ib/257 - tb/257

				tileScore += uint64(dr*dr + dg*dg + db*db)
			}
		}
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
	return int((float32(val) / float32(targetScale)) * float32(sourceScale))

}
