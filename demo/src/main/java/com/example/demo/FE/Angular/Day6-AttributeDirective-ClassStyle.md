# Day 6: Attribute Directives - Class & Style Binding

## Khái niệm cơ bản

**Attribute Directive** là loại directive thay đổi **cách hiển thị hoặc hành vi** của một DOM element, component, hoặc directive khác. Điểm khác biệt so với Structural Directive:

- **Structural Directive** (NgIf, NgFor, NgSwitch): Thêm/xóa/thay đổi cấu trúc DOM
- **Attribute Directive** (NgClass, NgStyle, NgModel): Thay đổi style, class, hoặc hành vi của element

Built-in Attribute Directives:
- `[class]` / `[class.className]` / `NgClass`
- `[style]` / `[style.propertyName]` / `NgStyle`
- `[(ngModel)]` (Two-way binding)

---

## Class Binding

### 1. Single Class Binding

Cú pháp đơn giản nhất: thêm hoặc xóa một class duy nhất

```html
<div [class.active]="isActive">
  {{ isActive ? "Active" : "Inactive" }}
</div>
```

Khi `isActive` là `true`, class `active` được thêm vào. Khi `false`, class bị xóa.

**Ví dụ thực tế: Tab selector**

```html
<button *ngFor="let tab of tabs; let i = index"
        [class.tab-active]="activeTabIndex === i"
        (click)="activeTabIndex = i">
  {{ tab.name }}
</button>
```

### 2. Multiple Classes - Property Binding

Khi muốn binding nhiều classes cùng lúc, dùng `[class]` với các dạng khác nhau:

#### Dạng String

```typescript
export class Component {
  cssClasses = "text-primary font-bold text-lg";
}
```

```html
<div [class]="cssClasses">
  Có 3 classes: text-primary, font-bold, text-lg
</div>
```

#### Dạng Array

```typescript
export class Component {
  cssClasses = ["btn", "btn-primary", "btn-lg"];
}
```

```html
<button [class]="cssClasses">
  Các classes từ array
</button>
```

#### Dạng Object

```typescript
export class Component {
  cssClasses = {
    "text-red": this.hasError,
    "text-green": this.isSuccess,
    "font-bold": true,
    "underline": false  // Class này sẽ không được áp dụng
  };
}
```

```html
<div [class]="cssClasses">
  Nếu key là truthy, class được thêm. Nếu falsy, bị xóa.
</div>
```

### 3. Kết hợp Static và Dynamic Class

Có thể vừa có static classes, vừa có dynamic:

```html
<div class="base-class" [class.active]="isActive">
  <!-- base-class luôn có, active được thêm khi isActive === true -->
</div>
```

Hoặc:

```html
<div [class]="'base-class ' + (isActive ? 'active' : '')">
  <!-- Kết hợp string -->
</div>
```

### 4. NgClass (Cách cũ)

Trước đây, `ngClass` được sử dụng để binding multiple classes:

```html
<!-- ❌ Cách cũ (vẫn hỗ trợ nhưng không khuyến cáo) -->
<div [ngClass]="{ 'active': isActive, 'disabled': isDisabled }">
  Content
</div>

<!-- ✅ Cách mới (khuyến cáo) -->
<div [class.active]="isActive" [class.disabled]="isDisabled">
  Content
</div>
```

**Tại sao dùng native binding thay vì ngClass?**
- Syntax rõ ràng, tiêu chuẩn HTML
- Hiệu suất tốt hơn (không cần gọi directive)
- Ít boilerplate code
- Align với Angular style guide mới

---

## Style Binding

### 1. Single Style Property

Cú pháp binding một style property duy nhất:

```html
<div [style.color]="dynamicColor">
  {{ dynamicColor }}
</div>
```

```typescript
export class Component {
  dynamicColor = "red";
}
```

### 2. Style Property với Unit

Khi muốn chỉ định unit (px, %, em, v.v.), dùng cú pháp:

```html
<div [style.width.px]="containerWidth">
  Width: {{ containerWidth }}px
</div>
```

```typescript
export class Component {
  containerWidth = 300;  // Không cần viết "300px"
}
```

**Các units hỗ trợ:**
- `px` - Pixels
- `%` - Percentage
- `em` - Em units
- `rem` - Root em units
- `vh` - Viewport height
- `vw` - Viewport width

### 3. Multiple Style Properties - String Format

```html
<div [style]="dynamicStyles">
  Inline styles từ string
</div>
```

```typescript
export class Component {
  dynamicStyles = "width: 100%; height: 50px; color: blue;";
}
```

### 4. Multiple Style Properties - Object Format

```html
<div [style]="styleObject">
  Inline styles từ object
</div>
```

```typescript
export class Component {
  styleObject = {
    'width': '100%',
    'height': '50px',
    'color': 'blue',
    'font-size': '16px'  // Hoặc fontSize
  };
}
```

### 5. Style Property Names - camelCase vs kebab-case

Angular hỗ trợ cả hai cách viết:

```html
<!-- Cách 1: camelCase -->
<div [style.fontSize]="fontSize">Camel Case</div>

<!-- Cách 2: kebab-case -->
<div [style.font-size]="fontSize">Kebab Case</div>
```

### 6. NgStyle (Cách cũ)

```html
<!-- ❌ Cách cũ (vẫn hỗ trợ nhưng không khuyến cáo) -->
<div [ngStyle]="{ 'color': color, 'font-size': fontSize }">
  Content
</div>

<!-- ✅ Cách mới (khuyến cáo) -->
<div [style.color]="color" [style.font-size.px]="fontSize">
  Content
</div>
```

---

## So sánh Cách cũ và Cách mới

| Khía cạnh | ngClass (Cũ) | [class] (Mới) |
|----------|---------------|---------------|
| Syntax | `[ngClass]="obj"` | `[class]="obj"` hoặc `[class.name]="bool"` |
| Performance | Cần directive processing | Native, nhanh hơn |
| Type Safety | Ít hỗ trợ type checking | Tốt hơn với TypeScript |
| Readability | Khó đọc với object | Rõ ràng, giống HTML standard |
| Import | Cần CommonModule | Không cần import |
| Deprecation | Sắp deprecated | Khuyến cáo sử dụng |

| Khía cạnh | ngStyle (Cũ) | [style] (Mới) |
|----------|--------------|--------------|
| Syntax | `[ngStyle]="obj"` | `[style.prop]="val"` hoặc `[style]="obj"` |
| Performance | Cần directive processing | Native, nhanh hơn |
| Units | Thủ công | `[style.prop.unit]="val"` tự động |
| Type Safety | Ít hỗ trợ | Tốt hơn |
| Deprecation | Sắp deprecated | Khuyến cáo sử dụng |

---

## Use Cases Thực Tế

### Use Case 1: Status Badge

```typescript
export class Component {
  status: 'success' | 'error' | 'warning' = 'success';
  
  getStatusClass() {
    return {
      'badge-success': this.status === 'success',
      'badge-error': this.status === 'error',
      'badge-warning': this.status === 'warning'
    };
  }
}
```

```html
<!-- Cách cũ -->
<span [ngClass]="getStatusClass()">{{ status }}</span>

<!-- Cách mới - Option 1 -->
<span [class.badge-success]="status === 'success'"
      [class.badge-error]="status === 'error'"
      [class.badge-warning]="status === 'warning'">
  {{ status }}
</span>

<!-- Cách mới - Option 2 (với CSS variable) -->
<span [class]="'badge badge-' + status">
  {{ status }}
</span>
```

### Use Case 2: Responsive Width

```typescript
export class Component {
  containerWidth = 80;
  containerHeight = 50;
}
```

```html
<!-- Cách cũ -->
<div [ngStyle]="{ 'width': containerWidth + '%', 'height': containerHeight + 'px' }">
  Container
</div>

<!-- Cách mới -->
<div [style.width.%]="containerWidth" [style.height.px]="containerHeight">
  Container
</div>
```

### Use Case 3: Dynamic Button Style

```typescript
export class Component {
  theme = 'dark';
  isDisabled = false;
  
  get buttonStyles() {
    return {
      'background-color': this.theme === 'dark' ? '#333' : '#fff',
      'color': this.theme === 'dark' ? '#fff' : '#333',
      'opacity': this.isDisabled ? '0.5' : '1',
      'cursor': this.isDisabled ? 'not-allowed' : 'pointer'
    };
  }
}
```

```html
<button [style]="buttonStyles" [disabled]="isDisabled">
  Click me
</button>
```

### Use Case 4: Form Validation Error Styling

```typescript
export class Component {
  form = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email])
  });
  
  get emailControl() {
    return this.form.get('email');
  }
}
```

```html
<input formControlName="email"
       [class.input-error]="emailControl?.invalid && emailControl?.touched"
       [style.border-color]="emailControl?.invalid ? 'red' : 'green'">

<div *ngIf="emailControl?.invalid && emailControl?.touched">
  Email is invalid
</div>
```

---

## Best Practices

### 1. Tránh Complex Logic trong Template

```html
<!-- ❌ KHÔNG: Logic phức tạp -->
<div [class]="{ 
  'class1': condition1 && condition2 || condition3,
  'class2': (value1 + value2) > threshold,
  'class3': array.length > 0 && array[0].status === 'active'
}">
  Content
</div>

<!-- ✅ CÓ: Đưa logic vào component -->
<div [class]="getClassObject()">
  Content
</div>
```

```typescript
export class Component {
  getClassObject() {
    return {
      'class1': this.isComplexCondition(),
      'class2': this.calculateSum() > this.threshold,
      'class3': this.isArrayActive()
    };
  }
  
  private isComplexCondition(): boolean {
    // Logic rõ ràng
    return this.condition1 && this.condition2 || this.condition3;
  }
}
```

### 2. Sử dụng Computed Properties (Angular 17+)

```typescript
import { Component, computed } from '@angular/core';

export class Component {
  isDisabled = signal(false);
  isLoading = signal(false);
  
  buttonClasses = computed(() => ({
    'btn-disabled': this.isDisabled(),
    'btn-loading': this.isLoading()
  }));
}
```

```html
<button [class]="buttonClasses()">Submit</button>
```

### 3. CSS Modules / BEM Naming

```typescript
export class Component {
  // Sử dụng BEM (Block, Element, Modifier)
  get cardClasses() {
    return {
      'card': true,
      'card--elevated': this.isElevated,
      'card__header': false,  // Đây là Element, không nên binding như class block
      'card--dark': this.isDarkMode
    };
  }
}
```

```html
<div [class]="cardClasses">
  <header class="card__header">Header</header>
  <div class="card__body">Body</div>
</div>
```

### 4. Kết hợp Static và Dynamic một cách hợp lý

```html
<!-- ✅ Tốt: Base classes static, logic classes dynamic -->
<div class="container container-base"
     [class.container-active]="isActive"
     [class.container-error]="hasError">
  Content
</div>

<!-- ❌ Không tốt: Tất cả dynamic -->
<div [class]="'container container-base ' + (isActive ? 'container-active' : '')">
  Content
</div>
```

### 5. Sử dụng ng-container khi cần

```html
<!-- Khi cần conditional styling trên nhiều elements -->
<ng-container *ngIf="showDetails">
  <div class="details">
    <p [class.highlight]="isHighlighted">Highlighted text</p>
    <p [style.color]="dynamicColor">Colored text</p>
  </div>
</ng-container>
```

---

## Performance Considerations

### Class Binding vs CSS Variables

```typescript
// ❌ Cách không tối ưu: Binding từng style
<div [style.color]="color"
     [style.background]="background"
     [style.border]="border"
     [style.padding]="padding"
     [style.margin]="margin">
</div>

// ✅ Cách tối ưu: Dùng CSS Variables
<div [style]="{ '--color': color, '--bg': background, '--border': border, '--pad': padding, '--margin': margin }">
</div>
```

```css
div {
  color: var(--color);
  background: var(--bg);
  border: var(--border);
  padding: var(--pad);
  margin: var(--margin);
}
```

### Avoid Inline Styles với Large Lists

```html
<!-- ❌ KHÔNG: Inline styles trên mỗi item -->
<div *ngFor="let item of items">
  <div [style.width]="item.width"
       [style.height]="item.height"
       [style.backgroundColor]="item.color">
    {{ item.name }}
  </div>
</div>

<!-- ✅ CÓ: Dùng class và CSS -->
<div *ngFor="let item of items" class="item">
  <div [class]="'item-type-' + item.type">
    {{ item.name }}
  </div>
</div>
```

```css
.item { /* Base styles */ }
.item-type-a { width: 100px; height: 50px; background: red; }
.item-type-b { width: 200px; height: 100px; background: blue; }
```

---

## Migration từ NgClass/NgStyle sang Native Bindings

### Step 1: Xác định mẫu sử dụng

```html
<!-- Pattern 1: Object binding -->
[ngClass]="{ 'class1': condition1, 'class2': condition2 }"
↓
[class.class1]="condition1" [class.class2]="condition2"

<!-- Pattern 2: String binding -->
[ngClass]="'class1 class2'"
↓
[class]="'class1 class2'"

<!-- Pattern 3: Array binding -->
[ngClass]="['class1', 'class2']"
↓
[class]="['class1', 'class2']"
```

### Step 2: Áp dụng Migration

```html
<!-- Trước -->
<div [ngClass]="{ 'active': isActive, 'disabled': isDisabled }"
     [ngStyle]="{ 'color': textColor, 'font-size': fontSize + 'px' }">
  Content
</div>

<!-- Sau -->
<div [class.active]="isActive"
     [class.disabled]="isDisabled"
     [style.color]="textColor"
     [style.font-size.px]="fontSize">
  Content
</div>
```

---

## Tóm tắt

| Khía cạnh | Chi tiết |
|----------|---------|
| **Mục đích** | Thay đổi style/class của element động |
| **Loại** | Attribute Directive |
| **Native Bindings** | `[class]`, `[class.name]`, `[style]`, `[style.name]` |
| **Directives (Cũ)** | `ngClass`, `ngStyle` |
| **Khuyến cáo** | Dùng native bindings |
| **Units** | `[style.prop.unit]="value"` |
| **Performance** | Native > Directive |
| **Type Safety** | Native tốt hơn |
| **Deprecation** | ngClass/ngStyle sắp deprecated |

---

## Tham khảo

- [Angular Class and Style Binding](https://angular.dev/guide/templates/class-binding)
- [Angular Attribute Directives](https://angular.dev/guide/directives/attribute-directives)
- [Style Guide: Prefer class and style](https://angular.dev/style-guide#prefer-class-and-style-over-ngclass-and-ngstyle)
- [NgClass Documentation](https://angular.dev/api/common/NgClass)
- [NgStyle Documentation](https://angular.dev/api/common/NgStyle)
