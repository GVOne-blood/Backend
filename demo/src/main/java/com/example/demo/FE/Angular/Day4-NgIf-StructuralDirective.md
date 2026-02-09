# Day 4: Structural Directives - NgIf (Điều kiện trong template)

## Khái niệm cơ bản

**Structural Directive** là loại directive thay đổi cấu trúc (structure) của DOM bằng cách thêm, xóa, hoặc thay đổi các phần tử. Khác với Attribute Directive (chỉ thay đổi hiển thị hay hành vi), Structural Directive thực sự thay đổi layout của trang.

**NgIf** là structural directive phổ biến nhất, cho phép hiển thị hoặc ẩn một phần tử dựa trên điều kiện boolean. Nó không chỉ ẩn (CSS `display: none`), mà hoàn toàn xóa phần tử khỏi DOM khi điều kiện sai.

### Tại sao gọi là "Structural"?

Vì nó thay đổi **structure** (cấu trúc) của DOM. Ví dụ, khi bạn viết:

```html
<div *ngIf="isVisible">Content</div>
```

Nếu `isVisible` là `false`, `<div>` này sẽ bị xóa hoàn toàn khỏi DOM, không chỉ bị ẩn. Nếu là `true`, nó sẽ xuất hiện.

---

## Nguyên tắc hoạt động

### Phía dưới: ng-template

Syntactic sugar `*ngIf` được chuyển đổi thành form dài hạn sử dụng `<ng-template>`:

```html
<!-- Cách viết ngắn (cái ta thường dùng) -->
<div *ngIf="condition">Content</div>

<!-- Cách viết dài (Angular compile thành như này) -->
<ng-template [ngIf]="condition">
  <div>Content</div>
</ng-template>
```

Angular thực hiện quá trình này gọi là **desugaring**. Cách viết ngắn được chuyển đổi thành `<ng-template>` vì lý do kỹ thuật:
- Chỉ một structural directive cho một phần tử
- `<ng-template>` không render trực tiếp (là placeholder)
- Structural directive kiểm soát khi nào render template

### Quy tắc "Một directive cho một phần tử"

```html
<!-- ❌ LỖI: Không thể có 2 structural directive cho 1 phần tử -->
<div *ngIf="condition" *ngFor="let item of items">
  {{ item }}
</div>

<!-- ✅ ĐÚNG: Dùng ng-container để wrap -->
<ng-container *ngIf="condition">
  <div *ngFor="let item of items">{{ item }}</div>
</ng-container>
```

Lý do: Nếu Angular dùng 2 `<ng-template>`, thứ tự nào được apply trước? Không rõ ràng, nên được cấm.

---

## NgIf - Cơ bản

### Cú pháp đơn giản

```html
<div *ngIf="expression">
  Nội dung hiển thị khi expression = true
</div>
```

**Expression** có thể là:
- Biến boolean: `*ngIf="isActive"`
- So sánh: `*ngIf="age >= 18"`
- Hàm trả boolean: `*ngIf="hasPermission()"`
- Bất kỳ giá trị truthy/falsy

### Truthy vs Falsy trong JavaScript/Angular

**Falsy:**
- `false`, `0`, `null`, `undefined`, `""` (chuỗi rỗng), `NaN`

**Truthy:**
- `true`, số khác 0, chuỗi không rỗng, object, array

```typescript
@Component({
  selector: 'app-permission',
  template: `
    <p *ngIf="user">Xin chào {{ user.name }}</p>
    <p *ngIf="!user">Vui lòng đăng nhập</p>
    <p *ngIf="count > 0">Có {{ count }} tin nhắn</p>
  `
})
export class PermissionComponent {
  user = null;        // Falsy
  count = 0;          // Falsy
}
```

Trong ví dụ trên, vì `user` là `null` (falsy), dòng "Xin chào" không hiển thị, nhưng "Vui lòng đăng nhập" sẽ hiển thị.

---

## NgIf-Else

### Cú pháp

```html
<div *ngIf="condition; else elseTemplate">
  Hiển thị khi true
</div>

<ng-template #elseTemplate>
  Hiển thị khi false
</ng-template>
```

`#elseTemplate` là **template reference variable** - cách đặt tên và tham chiếu template.

### Ví dụ thực tế

```typescript
@Component({
  selector: 'app-age-check',
  template: `
    <div *ngIf="user.age >= 13; else tooYoung">
      <p>Bạn có thể xem nội dung PG-13</p>
    </div>
    
    <ng-template #tooYoung>
      <p>Bạn chưa đủ tuổi để xem nội dung này</p>
    </ng-template>
  `
})
export class AgeCheckComponent {
  user = { age: 10 };
}
```

Vì `age = 10` (< 13), template `#tooYoung` sẽ được render.

---

## NgIf-Then-Else

Cú pháp này cho phép chỉ định rõ ràng template cho cả hai trường hợp:

```html
<ng-container *ngIf="condition; then thenTemplate; else elseTemplate"></ng-container>

<ng-template #thenTemplate>
  Hiển thị khi true
</ng-template>

<ng-template #elseTemplate>
  Hiển thị khi false
</ng-template>
```

### Khi nào dùng thì-else?

Thì-else hữu ích khi:
- Cả hai nội dung dài dòng
- Muốn tách biệt rõ ràng logic
- Nội dung có thể được tái sử dụng ở chỗ khác

---

## Ng-template là gì?

`<ng-template>` là **placeholder** - một phần tử được Angular xử lý nhưng **không bao giờ render trực tiếp** lên DOM.

### Đặc điểm

1. **Không render**: Nội dung bên trong không hiển thị trừ khi được structural directive hoặc code sử dụng
2. **Placeholder**: Lưu trữ template definition để dùng sau
3. **Không thêm wrapper**: Không tạo thêm `<div>` như các element thường

```html
<!-- Kết quả: Page không có gì hiển thị -->
<ng-template>
  <p>Text ẩn</p>
</ng-template>

<!-- Tương tự -->
<div style="display: none">
  <p>Text ẩn</p>
</div>
<!-- Nhưng ng-template thậm chí không có div trong DOM! -->
```

### Sử dụng ng-template

```html
<!-- Với ngIf -->
<p *ngIf="condition; else fallback">Thường</p>
<ng-template #fallback><p>Dự phòng</p></ng-template>

<!-- Với *ngTemplateOutlet (render động) -->
<ng-template #myTemplate>
  <p>Content</p>
</ng-template>

<ng-container *ngTemplateOutlet="myTemplate"></ng-container>
<!-- Kết quả: <p>Content</p> được render -->
```

---

## Comparing: Hiện/Ẩn vs Xóa/Thêm

### Ẩn với CSS (sai cách cho ngIf)

```html
<div [style.display]="isVisible ? 'block' : 'none'">
  Content
</div>
```

**Vấn đề:**
- Element vẫn trong DOM (có thể access qua JavaScript)
- Child components vẫn khởi tạo (waste tài nguyên)
- Kém hiệu quả nếu content phức tạp

### Xóa với NgIf (cách đúng)

```html
<div *ngIf="isVisible">
  Content
</div>
```

**Ưu điểm:**
- Element hoàn toàn bị loại bỏ khỏi DOM
- Child components bị destroy (tiết kiệm bộ nhớ)
- Lifecycle hooks được kích hoạt (ngOnInit, ngOnDestroy)

### Performance

Nếu có danh sách 1000 items nhưng chỉ 10 hiển thị:

**CSS `display: none`:**
- 1000 components đều khởi tạo ❌
- Bộ nhớ lớn ❌

**NgIf:**
- Chỉ 10 components khởi tạo ✓
- Bộ nhớ nhỏ ✓

---

## Thay đổi từ Angular cũ đến Angular 19+

### Angular cũ: Structural Directives

```html
<div *ngIf="isLoggedIn">
  Xin chào {{ user.name }}
</div>
<div *ngIf="!isLoggedIn">
  Vui lòng đăng nhập
</div>
```

**Nhược điểm:**
- Syntax phức tạp (ng-template, template reference)
- Cần import CommonModule
- Khó đọc với nhiều điều kiện

### Angular 17+: Control Flow Blocks

Angular 17 giới thiệu **Control Flow Blocks** - syntax mới, gọn gàng hơn:

```html
@if (isLoggedIn) {
  <div>Xin chào {{ user.name }}</div>
} @else {
  <div>Vui lòng đăng nhập</div>
}
```

**Ưu điểm:**
- Syntax rõ ràng, giống các ngôn ngữ lập trình
- Không cần import
- Ngắn gọn, dễ đọc
- Performance tốt hơn (built into compiler)

### So sánh cú pháp

| Tình huống | Cách cũ (*ngIf) | Cách mới (@if) |
|-----------|---------------|---|
| If đơn | `<div *ngIf="cond">...</div>` | `@if (cond) { ... }` |
| If-Else | `<div *ngIf="cond; else el"></div><ng-template #el>...</ng-template>` | `@if (cond) { ... } @else { ... }` |
| If-Else If | Phức tạp (nested templates) | `@if (cond1) { ... } @else if (cond2) { ... } @else { ... }` |
| Một phần tử, 2 directive | Cần `<ng-container>` | Không cần wrapper |

### Nên dùng cách nào?

**Dùng `@if` (Angular 17+):**
- Dự án mới, độc lập
- Muốn code sạch, maintainable
- Performance quan trọng

**Giữ `*ngIf`:**
- Dự án cũ, phải compatible
- Team không ready upgrade
- Chưa update lên Angular 17+

---

## Best Practices

### 1. Tránh logic phức tạp trong template

**❌ Sai:**
```html
<div *ngIf="user && user.age > 18 && user.permissions.includes('view')">
  Content
</div>
```

**✅ Đúng:**
```typescript
export class Component {
  canViewContent(): boolean {
    return this.user?.age > 18 && 
           this.user?.permissions?.includes('view');
  }
}

<!-- Template -->
<div *ngIf="canViewContent()">Content</div>
```

### 2. Sử dụng Async Pipe với Observable

```typescript
export class Component {
  isLoading$ = this.service.isLoading$;
}

<!-- Template - tránh subscription leak -->
<div *ngIf="isLoading$ | async">Loading...</div>
```

### 3. Tránh gọi function nhiều lần

**❌ Sai (được gọi lại mỗi change detection):**
```html
<div *ngIf="expensiveCheck()">...</div>
```

**✅ Đúng (tính một lần):**
```typescript
export class Component {
  isValid = this.expensiveCheck();
}
```

### 4. Sử dụng ng-container cho wrapper không cần style

```html
<!-- ❌ Thêm div không cần thiết -->
<div *ngIf="condition">
  <p>Item 1</p>
  <p>Item 2</p>
</div>

<!-- ✅ Dùng ng-container -->
<ng-container *ngIf="condition">
  <p>Item 1</p>
  <p>Item 2</p>
</ng-container>
```

---

## Lifecycle với NgIf

Khi element được thêm/xóa bởi ngIf, lifecycle hooks được kích hoạt:

```typescript
@Component({
  selector: 'app-child',
  template: '<p>Child</p>'
})
export class ChildComponent implements OnInit, OnDestroy {
  ngOnInit() {
    console.log('Child initialized'); // Kích hoạt khi *ngIf=true
  }
  
  ngOnDestroy() {
    console.log('Child destroyed'); // Kích hoạt khi *ngIf=false
  }
}

@Component({
  selector: 'app-parent',
  template: `
    <button (click)="toggle()">Toggle</button>
    <app-child *ngIf="isVisible"></app-child>
  `
})
export class ParentComponent {
  isVisible = true;
  
  toggle() {
    this.isVisible = !this.isVisible;
    // Mỗi lần toggle, child được init/destroy
  }
}
```

---

## Tóm tắt

| Khía cạnh | Mô tả |
|---------|--------|
| **Mục đích** | Điều kiện hiển thị/ẩn phần tử |
| **Loại** | Structural Directive |
| **Cách cũ** | `*ngIf="condition"` |
| **Cách mới** | `@if (condition) { }` |
| **Template** | `<ng-template>` không render trực tiếp |
| **Một phần tử, nhiều directive** | Dùng `<ng-container>` |
| **Performance** | Xóa khỏi DOM > CSS `display:none` |
| **Lifecycle** | Init/Destroy khi add/remove |

---

## Tham khảo

- [Angular Structural Directives Guide](https://angular.dev/guide/directives/structural-directives)
- [Angular Control Flow Syntax](https://angular.dev/guide/templates/control-flow)
- [ng-template Documentation](https://angular.dev/guide/templates/ng-template)
