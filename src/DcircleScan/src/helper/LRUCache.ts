export class LRUCache<TKey, TValue> {
  private readonly items: Map<TKey, TValue>;
  private readonly capacity: number;

  constructor(capacity: number) {
    this.capacity = capacity;
    this.items = new Map<TKey, TValue>();
  }

  get(key: TKey): TValue | undefined {
    const item = this.items.get(key);
    if (item === undefined) {
      return undefined;
    }

    this.items.delete(key);
    this.items.set(key, item);
    return item;
  }

  set(key: TKey, value: TValue): void {
    if (this.items.size >= this.capacity) {
      const firstKey = this.items.keys().next().value;
      this.items.delete(firstKey);
    }
    this.items.set(key, value);
  }
}
