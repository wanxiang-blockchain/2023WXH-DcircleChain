

export class ConnError implements Error{
  message: string
  name: string
  stack?: string

  constructor(error: Error) {
    this.message = error.message
    this.name = error.name
    this.stack = error.stack
  }
}