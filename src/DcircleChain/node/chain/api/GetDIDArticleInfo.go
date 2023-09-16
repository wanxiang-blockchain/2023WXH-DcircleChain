package api

import (
	"context"
	"dcircleserver/src/im/db"
	"dcircleserver/src/im/db/attachment"
	"errors"
	"github.com/xpwu/go-log/log"
)

type GetDIDArticleInfoRequest struct {
	Address string `json:"address"`
	Version int    `json:"version"`
}

type GetDIDArticleInfoResponse struct {
	Address        string             `json:"address"`
	Title          string             `json:"title"`
	Abstract       string             `json:"abstract"`
	Token          string             `json:"token"`
	Version        int                `json:"version"`
	InitCode       int                `json:"initCode"`
	Salt           string             `json:"salt"`
	FeatureVale    []string           `json:"featureVale"`
	CreateTime     int64              `json:"createTime"`
	CreatorAddress string             `json:"creatorAddress"`
	UpdateTime     int64              `json:"updateTime"`
	AbstractImage  []attachment.Image `json:"abstractImage"`
}

func (s *suite) APIGetDIDArticleInfo(ctx context.Context, request *GetDIDArticleInfoRequest) *GetDIDArticleInfoResponse {
	_, logger := log.WithCtx(ctx)
	logger.PushPrefix("GetDIDArticleInfo")
	if len(request.Address) == 0 {
		logger.Debug("address is empty")
		s.Request.Terminate(errors.New("address is empty"))
	}

	//return GetTestData(request.Address)
	oneArticle, err := db.NewDIDArticle(ctx).FindByAddress(request.Address)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}

	if len(oneArticle.Address) == 0 {
		logger.Debug("didn't find this article:", request.Address)
		return &GetDIDArticleInfoResponse{FeatureVale: []string{}}
	}

	if oneArticle.IsDelete() {
		logger.Warning("deleted this article:", request.Address)
		return &GetDIDArticleInfoResponse{FeatureVale: []string{}}
	}

	var articleBlock *db.DIDArticleBlockDocument
	if request.Version > 0 {
		articleBlock, err = db.NewDIDArticleBlock(ctx).FindByDidAddressAndVersion(request.Address, request.Version)
	} else {
		articleBlock, err = db.NewDIDArticleBlock(ctx).FindByRootHash(oneArticle.CurrentBlockRootHash)
	}

	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}

	arMeta, err := db.GetDidBlockMetaInfo(ctx, articleBlock.ARMetaRootHash)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}

	crMeta, err := db.GetDidBlockMetaInfo(ctx, articleBlock.EncRootHash)
	if err != nil {
		logger.Error(err)
		s.Request.Terminate(err)
	}

	initCode := articleBlock.InitCode
	salt := articleBlock.Salt
	if articleBlock.Version != db.GenesisBlockVersion {
		initBlock, err := db.NewDIDArticleBlock(ctx).FindByDidAddressAndVersionNoStatus(request.Address, db.GenesisBlockVersion)
		if err != nil {
			logger.Error(err)
		} else {
			initCode = initBlock.InitCode
			salt = initBlock.Salt
		}
	}

	res := &GetDIDArticleInfoResponse{
		Address:        oneArticle.Address,
		Title:          crMeta.Title.Text,
		Abstract:       arMeta.Title.Text,
		Token:          articleBlock.TokenAddress,
		Version:        articleBlock.Version,
		InitCode:       initCode,
		Salt:           salt,
		FeatureVale:    []string{},
		CreateTime:     int64(oneArticle.CreateTime),
		CreatorAddress: oneArticle.CreatorUid,
		UpdateTime:     articleBlock.CreateTime,
		AbstractImage:  []attachment.Image{},
	}

	for _, node := range arMeta.Content {
		res.AbstractImage = append(res.AbstractImage, node.ToImage())
	}

	for _, node := range crMeta.Content {
		res.FeatureVale = append(res.FeatureVale, node.Hash)
	}

	return res
}
