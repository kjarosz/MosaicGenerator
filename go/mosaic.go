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
	debug      bool
	tileSize   image.Point
	cellSize   image.Point
	usePenalty int64
}

type tile struct {
	tileSized *image.RGBA
	cellSized *image.RGBA
	useCount  int64
}

type selectedTile struct {
	tile     *image.RGBA
	position image.Point
}

func createMosaic(img image.Image, sourceTiles []image.Image, params MosaicParameters) image.Image {
	tiles := makeTileBank(sourceTiles, params.tileSize, params.cellSize)
	if params.debug {
		saveTiles(tiles)
	}

	originalBounds := img.Bounds()
	var gridDimensions image.Point
	gridDimensions.X = int(math.Ceil(float64(originalBounds.Max.X) / float64(params.cellSize.X)))
	gridDimensions.Y = int(math.Ceil(float64(originalBounds.Max.Y) / float64(params.cellSize.Y)))
	scaledImage := rescaleImage(img, gridDimensions.X*params.cellSize.X, gridDimensions.Y*params.cellSize.Y)

	tileChannel := make(chan selectedTile, 10)

	go func(tileChannel chan<- selectedTile) {
		for x := 0; x < gridDimensions.X; x++ {
			for y := 0; y < gridDimensions.Y; y++ {
				subRect := image.Rect(
					x*params.cellSize.X,
					y*params.cellSize.Y,
					(x+1)*params.cellSize.X,
					(y+1)*params.cellSize.Y)

				var t selectedTile
				t.tile = findClosestTile(scaledImage, subRect, tiles, params.usePenalty)
				t.position.X = x
				t.position.Y = y
				tileChannel <- t
			}
		}
		close(tileChannel)
	}(tileChannel)

	mosaic := image.NewRGBA(image.Rect(0, 0, gridDimensions.X*params.tileSize.X, gridDimensions.Y*params.tileSize.Y))
	i := 0
	maxI := gridDimensions.X * gridDimensions.Y
	for tile := range tileChannel {
		draw.Draw(
			mosaic,
			image.Rect(
				tile.position.X*params.tileSize.X,
				tile.position.Y*params.tileSize.Y,
				(tile.position.X+1)*params.tileSize.X,
				(tile.position.Y+1)*params.tileSize.Y),
			tile.tile,
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

func makeTileBank(sourceTiles []image.Image, tileSize image.Point, cellSize image.Point) []tile {
	var tiles []tile
	tiles = make([]tile, len(sourceTiles))

	for i, sourceTile := range sourceTiles {
		tiles[i].tileSized = rescaleImage(sourceTile, tileSize.X, tileSize.Y)
		tiles[i].cellSized = rescaleImage(sourceTile, cellSize.X, cellSize.Y)
	}

	return tiles
}

func saveTiles(tiles []tile) {
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

		err = jpeg.Encode(file, tile.tileSized, nil)
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

func findClosestTile(originalImage *image.RGBA, subRect image.Rectangle, tiles []tile, usePenalty int64) *image.RGBA {
	var minScore int64
	minScore = math.MaxInt64
	tileIndex := -1

	totalPixels := int64(subRect.Dx() * subRect.Dy())
	for i, tile := range tiles {
		var r, g, b int64
		for x := 0; x < subRect.Dx(); x++ {
			for y := 0; y < subRect.Dy(); y++ {
				tPixel := tile.cellSized.PixOffset(x, y)
				iPixel := originalImage.PixOffset(subRect.Min.X+x, subRect.Min.Y+y)

				tr, tg, tb := tile.cellSized.Pix[tPixel], tile.cellSized.Pix[tPixel+1], tile.cellSized.Pix[tPixel+2]
				ir, ig, ib := originalImage.Pix[iPixel], originalImage.Pix[iPixel+1], originalImage.Pix[iPixel+2]

				dr := int64(ir) - int64(tr)
				dg := int64(ig) - int64(tg)
				db := int64(ib) - int64(tb)

				r += dr * dr
				g += dg * dg
				b += db * db
			}
		}
		tileScore := int64(math.Sqrt(float64(r/totalPixels + g/totalPixels + b/totalPixels)))
		tileScore += tile.useCount * usePenalty
		if tileScore < minScore {
			minScore = tileScore
			tileIndex = i
		}
	}

	if tileIndex < 0 {
		log.Fatal("Could not find a tile to fill in for image.")
	}

	tiles[tileIndex].useCount++
	return tiles[tileIndex].tileSized
}

func nearestNeighbor(val int, targetScale int, sourceScale int) int {
	return int((float32(val) / float32(sourceScale)) * float32(targetScale))
}

func nearestTargetNeighbor(val int, targetScale int, sourceScale int) int {
	return int((float32(val) / float32(targetScale)) * float32(sourceScale))
}
