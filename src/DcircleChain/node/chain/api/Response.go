package api

type Result = int64

const (
	ResultFail    = -1
	ResultSuccess = 1
)

type Response struct {
	Result Result `json:"result"`
}

func SucceedResponse() *Response {
	return &Response{Result: ResultSuccess}
}

func FailedResponse() *Response {
	return &Response{Result: ResultFail}
}
