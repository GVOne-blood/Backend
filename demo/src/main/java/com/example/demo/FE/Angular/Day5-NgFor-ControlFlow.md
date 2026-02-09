# Day 5: Structural Directives - NgFor / @for (Vòng lặp trong template)

## Khái niệm cơ bản

**NgFor** (hay NgForOf) là một structural directive cho phép bạn lặp lại (iterate) qua một tập hợp dữ liệu (thường là mảng) và tạo ra DOM element tương ứng cho mỗi phần tử. Đây là cách chính để hiển thị danh sách động trong Angular.

Khi sử dụng NgFor, Angular sẽ:
1. Lấy từng phần tử từ mảng
2. Tạo một template instance cho mỗi phần tử
3. Gắn dữ liệu của phần tử vào instance đó
4. Render ra DOM

Khác với CSS `display: none`, NgFor hoàn toàn xóa/thêm phần tử khỏi DOM, do đó có hiệu suất tốt hơn khi dữ liệu lớn.

---

## Nguyên tắc hoạt động

### Tại sao gọi là "for" trong "ngFor"?

Vì syntax của nó giống `for...of` loop trong JavaScript:

```javascript
// JavaScript
for (let author of authors) {
  console.log(author);
}

// Angular template (tương tự)
<div *ngFor="let author of authors">
  {{ author }}
</div>
```

### Phía dưới: ng-template

Như NgIf, NgFor cũng là syntactic sugar được chuyển đổi thành `<ng-template>`:

```html
<!-- Cách viết ngắn -->
<div *ngFor="let item of items">
  {{ item }}
</div>

<!-- Cách viết dài (Angular compile thành như này) -->
<ng-template ngFor [ngForOf]="items" let-item>
  <div>{{ item }}</div>
</ng-template>
```

Desugaring này cho phép Angular kiểm soát khi nào render, với data nào.

---

## NgFor - Local Variables

Một trong những tính năng mạnh nhất của NgFor là nó cung cấp các biến cục bộ (local variables) để truy cập thông tin bổ sung về mỗi iteration.

### Các biến có sẵn

| Biến | Kiểu | Mô tả |
|------|------|--------|
| **$implicit** | T | Giá trị của phần tử hiện tại (giá trị này gắn cho biến khi khai báo `let something of xxx`) |
| **index** | number | Chỉ số (0-based) của phần tử hiện tại |
| **count** | number | Tổng số phần tử trong danh sách |
| **first** | boolean | True nếu đây là phần tử đầu tiên |
| **last** | boolean | True nếu đây là phần tử cuối cùng |
| **even** | boolean | True nếu index là số chẵn (0, 2, 4, ...) |
| **odd** | boolean | True nếu index là số lẻ (1, 3, 5, ...) |

### Cú pháp truy cập

```html
<div *ngFor="let author of authors; let idx = index; let total = count">
  ({{ idx }}/{{ total }}): {{ author.name }}
</div>
```

Cú pháp `let variable = localVariableName` cho phép tạo alias cho biến local.

### Ví dụ sử dụng

```html
<ul>
  <li *ngFor="let item of items; let i = index; let last = last">
    <!-- Hiển thị số thứ tự -->
    #{{ i + 1 }}: {{ item.name }}
    
    <!-- Thêm dấu phân cách giữa các item -->
    <span *ngIf="!last">, </span>
  </li>
</ul>
```

---

## Một directive cho một phần tử

### Quy tắc cơ bản

Bạn **không thể** đặt 2 structural directives trên cùng một phần tử:

```html
<!-- ❌ LỖI: Không thể có *ngFor và *ngIf cùng lúc -->
<div *ngFor="let item of items" *ngIf="item.active">
  {{ item }}
</div>
```

### Giải pháp: Sử dụng ng-container

Để kết hợp nhiều structural directives, sử dụng `<ng-container>` làm wrapper:

```html
<!-- ✅ ĐÚNG: Wrapper với ng-container -->
<ng-container *ngFor="let item of items">
  <div *ngIf="item.active">
    {{ item }}
  </div>
</ng-container>
```

Hoặc ngược lại:

```html
<ng-container *ngIf="showList">
  <div *ngFor="let item of items">
    {{ item }}
  </div>
</ng-container>
```

**Tại sao?** `<ng-container>` không render thành DOM element, nó chỉ là logical container cho directive.

---

## TrackBy - Performance Optimization

### Vấn đề performance

Mặc định, khi mảng `items` thay đổi, Angular sẽ:
1. Destroy tất cả DOM elements cũ
2. Tạo lại từ đầu
3. Re-initialize tất cả components

Với danh sách lớn, điều này rất tốn resources.

### Giải pháp: trackBy

`trackBy` cho phép Angular biết được **cách xác định danh tính duy nhất** của mỗi phần tử. Nếu phần tử có danh tính không đổi, Angular chỉ cập nhật bindings mà không destroy/recreate.

### Cú pháp

```html
<div *ngFor="let item of items; trackBy: trackByFn">
  {{ item }}
</div>
```

```typescript
export class Component {
  items = [
    { id: 1, name: 'Item 1' },
    { id: 2, name: 'Item 2' },
    { id: 3, name: 'Item 3' }
  ];
  
  // Function để xác định danh tính
  trackByFn(index: number, item: any): any {
    return item.id;  // Dùng id làm unique identifier
  }
}
```

### Khi nào dùng trackBy

- Danh sách lớn (100+ items)
- Items có components con phức tạp
- Items được cập nhật thường xuyên

---

## Thay đổi từ Angular cũ đến Angular 19+

### Angular cũ: Structural Directives

```html
<ul>
  <li *ngFor="let item of items; let i = index">
    {{ i + 1 }}: {{ item.name }}
  </li>
</ul>
```

**Nhược điểm:**
- Syntax khó nhớ (biến local phức tạp)
- Cần import CommonModule
- trackBy cộng thêm

### Angular 17+: Control Flow Blocks

Angular 17 giới thiệu `@for` block - cách mới gọn gàng hơn:

```html
<ul>
  @for (item of items; track item.id; let i = $index) {
    <li>{{ i + 1 }}: {{ item.name }}</li>
  }
</ul>
```

**Ưu điểm:**
- Syntax rõ ràng, giống JavaScript
- Không cần import
- `track` bắt buộc (tốt cho performance)
- Biến local tự động (`$index`, `$first`, `$last`, v.v.)
- Có `@empty` block cho danh sách rỗng

### So sánh chi tiết

| Khía cạnh | *ngFor | @for |
|----------|--------|------|
| Syntax | `*ngFor="let x of items; let i = index"` | `@for (x of items; track x.id; let i = $index)` |
| Import | Cần CommonModule | Không cần import |
| TrackBy | Tùy chọn | **Bắt buộc** |
| Local variables | `index`, `count`, `first`, `last` | `$index`, `$count`, `$first`, `$last` |
| Empty state | Cần `*ngIf` riêng | `@empty` block |
| Performance | Tốt | **Tốt hơn** |
| Type-safe | Có | **Có + type narrowing** |

### Ví dụ @for với @empty

```html
<ul>
  @for (item of items; track item.id) {
    <li>{{ item.name }}</li>
  } @empty {
    <li>Không có item nào</li>
  }
</ul>
```

So sánh với cách cũ:

```html
<ul *ngIf="items.length > 0">
  <li *ngFor="let item of items; trackBy: trackByFn">
    {{ item.name }}
  </li>
</ul>
<ul *ngIf="items.length === 0">
  <li>Không có item nào</li>
</ul>
```

---

## Context Variables - Biến context

### @for context variables (Angular 17+)

```html
@for (item of items; track item.id; let i = $index) {
  <!-- $index: 0-based index -->
  <!-- $count: tổng số items -->
  <!-- $first: true nếu là item đầu -->
  <!-- $last: true nếu là item cuối -->
  <!-- $even: true nếu index chẵn -->
  <!-- $odd: true nếu index lẻ -->
  <div [class.striped]="$even">
    {{ $index + 1 }}/{{ $count }}: {{ item.name }}
  </div>
}
```

### ngFor context variables (cách cũ)

```html
<div *ngFor="let item of items; let i = index; let last = last">
  {{ i + 1 }}: {{ item.name }}
  <span *ngIf="!last">,</span>
</div>
```

---

## Identity và Change Detection

### Vấn đề identity

```typescript
export class Component {
  items = [1, 2, 3];
  
  addItem() {
    this.items.push(4);  // ❌ Modify array in place
  }
  
  addItem2() {
    this.items = [...this.items, 4];  // ✅ Create new array
  }
}
```

Ngay cả khi data thay đổi, nếu array reference giống nhau, Angular có thể không detect được.

### Quy tắc: Immutability

Luôn tạo array mới thay vì modify in place:

```typescript
// ❌ KHÔNG: Modify array
items.push(newItem);

// ✅ CÓ: Tạo array mới
this.items = [...this.items, newItem];

// ✅ CÓ: Sử dụng map
this.items = this.items.map(item => 
  item.id === id ? {...item, ...updated} : item
);
```

---

## Common Use Cases

### Danh sách bảng

```html
<table>
  <tbody>
    @for (row of data; track row.id) {
      <tr>
        <td>{{ row.name }}</td>
        <td>{{ row.email }}</td>
      </tr>
    } @empty {
      <tr><td colspan="2">Không có dữ liệu</td></tr>
    }
  </tbody>
</table>
```

### Lồng danh sách

```html
@for (category of categories; track category.id) {
  <div>
    <h3>{{ category.name }}</h3>
    @for (item of category.items; track item.id) {
      <div>{{ item.name }}</div>
    }
  </div>
}
```

### Với condition

```html
@for (item of items; track item.id) {
  @if (item.active) {
    <div class="active">{{ item.name }}</div>
  } @else {
    <div class="inactive">{{ item.name }}</div>
  }
}
```

---

## Best Practices

### 1. Luôn sử dụng trackBy / track

```html
<!-- ❌ Sai -->
<div *ngFor="let item of items">{{ item }}</div>

<!-- ✅ Đúng -->
<div *ngFor="let item of items; trackBy: trackByFn">{{ item }}</div>

<!-- ✅ Đúng (Angular 17+) -->
@for (item of items; track item.id) {
  <div>{{ item }}</div>
}
```

### 2. Tránh complex logic trong loop

```html
<!-- ❌ Sai -->
@for (item of items | filter: searchTerm | sort; track item.id) {
  <div>{{ item.name }}</div>
}

<!-- ✅ Đúng -->
@for (item of filteredItems; track item.id) {
  <div>{{ item.name }}</div>
}
```

```typescript
export class Component {
  filteredItems = computed(() => 
    this.items()
      .filter(i => i.name.includes(this.search()))
      .sort()
  );
}
```

### 3. Sử dụng unique identifier cho track

```html
<!-- ❌ Sai: index không phải unique identifier -->
@for (item of items; track $index) { }

<!-- ✅ Đúng: id là truly unique -->
@for (item of items; track item.id) { }

<!-- ✅ Cũng được: nếu không có id -->
@for (item of items; track item) { }
```

### 4. Phân tách component cho item

```html
<!-- ❌ Sai: quá nhiều logic -->
@for (item of items; track item.id) {
  <div class="complex">
    <h3>{{ item.title }}</h3>
    <!-- 10 dòng HTML -->
  </div>
}

<!-- ✅ Đúng: tách thành component -->
@for (item of items; track item.id) {
  <app-item [data]="item"></app-item>
}
```

---

## Performance Considerations

### Danh sách động vs Static

**Dynamic (thay đổi thường xuyên):**
- Cần trackBy/track
- Kiểm tra lại mỗi thay đổi
- Signals giúp optimize

**Static (không thay đổi):**
- TrackBy ít quan trọng
- Có thể dùng `OnPush` change detection

### Memory Usage

Mỗi item tạo một component instance. Với 1000 items phức tạp = 1000 instances = nhiều bộ nhớ.

**Giải pháp:**
- Virtual scrolling (CDK)
- Pagination
- Lazy loading
- TrackBy để tái sử dụng DOM

---

## Tóm tắt

| Khía cạnh | Chi tiết |
|----------|---------|
| **Mục đích** | Lặp qua array, render nhiều elements |
| **Loại** | Structural Directive |
| **Cách cũ** | `*ngFor="let x of items; trackBy: fn"` |
| **Cách mới** | `@for (x of items; track x.id)` |
| **Local variables** | `index`, `count`, `first`, `last`, `even`, `odd` |
| **Performance** | TrackBy bắt buộc để tái sử dụng DOM |
| **Empty state** | Cách cũ: `*ngIf`, Cách mới: `@empty` |
| **Quy tắc** | Một directive trên một element |
| **Best practice** | Tránh complex logic, dùng computed properties |

---

## Tham khảo

- [Angular Structural Directives](https://angular.dev/guide/directives/structural-directives)
- [NgFor Documentation](https://angular.dev/api/common/NgFor)
- [Control Flow @for Block](https://angular.dev/guide/templates/control-flow)
- [Virtual Scrolling CDK](https://material.angular.io/cdk/scrolling/overview)
