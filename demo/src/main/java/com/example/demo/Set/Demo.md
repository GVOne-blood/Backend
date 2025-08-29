# Set

## 1. Nêu ra các đặc điểm Set Interface

## 2. Kể ra các class triển khai từ Set Interface

## 3. Phân biệt rõ trường hợp sử dụng của từng class đó

[Chú thích](#chú-thích)

## Set

> Set là một tập hợp không cho phép chứa các phần tử trùng lặp.

Vì các phần tử là unique nên Set chỉ chưa tối đa 1 phần tử null

Các phuông thức chính :

- boolean add(E e): Thêm phần tử vào Set. Trả về true nếu Set thay đổi (phần tử chưa tồn tại), false nếu phần tử đã tồn
  tại.
- boolean remove(Object o): Xóa phần tử khỏi Set.
- boolean contains(Object o): Kiểm tra xem phần tử có tồn tại trong Set không.
- int size(): Trả về số lượng phần tử.
- boolean isEmpty(): Kiểm tra Set có rỗng không.
- void clear(): Xóa tất cả các phần tử.

### Chú thích