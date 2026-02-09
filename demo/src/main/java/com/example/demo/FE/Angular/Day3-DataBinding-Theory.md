# Day 3: Data Binding trong Angular

## Khái niệm cơ bản

**Data Binding** là cơ chế liên kết dữ liệu giữa component logic (TypeScript class) và template HTML. Nó cho phép dữ liệu từ component tự động được hiển thị, cập nhật hoặc đáp ứng sự kiện trong template mà không cần viết các đoạn code trung gian phức tạp.

Hãy hiểu data binding như một cây cầu hai chiều giữa dữ liệu (ở TypeScript) và giao diện (ở HTML). Khi bạn thay đổi dữ liệu ở component, giao diện tự động cập nhật. Khi người dùng tương tác với giao diện, dữ liệu component cũng thay đổi.

### Lịch sử và tiến hóa

Trước Angular, các framework khác như AngularJS cũng có khái niệm data binding nhưng không hiệu quả về hiệu năng. Angular (phiên bản 2 trở đi) đã cải thiện đáng kể bằng cách sử dụng **Change Detection Strategy** để chỉ cập nhật những phần thực sự thay đổi.

Từ Angular 19 trở đi, Angular giới thiệu **Signals** - một cách mới để quản lý state reactively, thay thế dần các Observable truyền thống cho các trường hợp đơn giản.

---

## 4 Loại Data Binding

Angular hỗ trợ 4 loại data binding chính, mỗi loại phục vụ một mục đích khác nhau:

| Loại | Hướng dữ liệu | Cú pháp | Mục đích |
|------|---------------|---------|---------|
| **Interpolation** | Một chiều: Component → Template | `{{ }}` | Hiển thị dữ liệu văn bản từ component |
| **Property Binding** | Một chiều: Component → Template | `[ ]` | Gán giá trị component vào thuộc tính DOM |
| **Event Binding** | Một chiều: Template → Component | `( )` | Lắng nghe sự kiện từ template, thực thi method |
| **Two-way Binding** | Hai chiều: Component ↔ Template | `[( )]` | Đồng bộ hóa dữ liệu cả hai chiều |

### Nguyên tắc hoạt động tổng quát

Data binding trong Angular hoạt động dựa trên **Change Detection**. Mỗi khi một sự kiện xảy ra (timer, event, network request, v.v.), Angular sẽ quét toàn bộ component tree để tìm những thay đổi và cập nhật DOM tương ứng.

---

## 1. Interpolation (Nội suy)

### Định nghĩa

Interpolation là cách để chèn giá trị của biến hoặc kết quả của một expression trực tiếp vào template. Nó cho phép bạn hiển thị dữ liệu từ component lên HTML theo dạng văn bản.

### Cơ chế hoạt động

Khi Angular compile template, nó sẽ:
1. Nhận dạng các interpolation expressions `{{ ... }}`
2. Dịch chúng thành property binding + method call
3. Mỗi khi change detection chạy, Angular tính toán expression này
4. Nếu giá trị thay đổi, cập nhật DOM

### Cú pháp

```html
{{ expression }}
```

Biểu thức bên trong có thể là:
- **Biến đơn giản**: `{{ name }}`
- **Thuộc tính của object**: `{{ user.firstName }}`
- **Phép tính toán**: `{{ 10 + 5 }}`, `{{ quantity * price }}`
- **Gọi method**: `{{ formatDate(date) }}`
- **Ternary operator**: `{{ isActive ? 'On' : 'Off' }}`
- **Logical operators**: `{{ hasPermission && canEdit }}`

### Ví dụ áp dụng

```typescript
@Component({
  selector: 'app-greeting',
  template: `
    <p>{{ message }}</p>
    <p>{{ user.fullName }}</p>
    <p>{{ 5 + 3 }}</p>
  `
})
export class GreetingComponent {
  message = 'Hello World';
  user = { fullName: 'John Doe' };
}
```

Trong ví dụ trên, mỗi lần `message` hoặc `user.fullName` thay đổi, Angular sẽ tự động cập nhật nội dung trong `<p>`.

### Giới hạn và điều cần tránh

Mặc dù interpolation rất linh hoạt, nhưng có những thứ **không nên** làm trong interpolation:

- **Không khai báo biến**: `{{ let x = 5 }}` ❌
- **Không gán giá trị**: `{{ a = 5 }}` ❌
- **Không sử dụng regex literals**: `{{ /test/.exec(input) }}` ❌
- **Không import module**: `{{ import('module') }}` ❌

Các hoạt động này sẽ gây lỗi compile hoặc runtime error.

### Performance consideration

Interpolation được tính toán lại **mỗi lần change detection chạy**. Nếu expression phức tạp hoặc tính toán nặng, điều này có thể ảnh hưởng đến hiệu năng. Trong những trường hợp này, tốt hơn là tính toán ở component và lưu kết quả vào một biến.

**Không tốt:**
```typescript
<p>{{ expensiveCalculation() }}</p>  // Được gọi hàng lần mỗi khi change detection
```

**Tốt hơn:**
```typescript
export class Component {
  result = this.expensiveCalculation();  // Tính 1 lần
}
// Template: <p>{{ result }}</p>
```

---

## 2. Property Binding

### Định nghĩa

Property binding cho phép bạn gán giá trị từ component vào một **property** (thuộc tính) của DOM element hoặc Angular component. Đây là cách chính để thay đổi hành vi, trạng thái hay nội dung của element.

### Attribute vs Property - Sự khác biệt quan trọng

Đây là một điểm nhầm lẫn thường gặp:

**Attribute (Thuộc tính HTML):**
- Được định nghĩa trong HTML source code: `<input type="text" value="hello">`
- Không thay đổi sau khi element được tạo
- Được sử dụng để khởi tạo DOM element

**Property (Thuộc tính DOM):**
- Là thuộc tính của JavaScript object tương ứng với element
- Có thể thay đổi động
- Thể hiện trạng thái hiện tại của element

**Ví dụ:**
```html
<input type="text" value="hello" />
```

Khi HTML load, `value` là attribute. Sau khi user nhập dữ liệu, `element.value` (property) thay đổi, nhưng attribute vẫn là `"hello"`.

### Cú pháp

```html
[propertyName]="expression"
```

Dấu ngoặc vuông `[ ]` báo cho Angular biết rằng đây là property binding.

### Tại sao Property Binding quan trọng

Property binding được sử dụng vì nó:
1. **Cập nhật động**: Có thể thay đổi property mà không cần reload page
2. **Thay đổi trạng thái UI**: Ví dụ disable button, show/hide element
3. **Bind component data**: Truyền dữ liệu giữa các component
4. **Bind CSS class/style**: Thay đổi styling động dựa trên state

### Ví dụ cơ bản

```typescript
@Component({
  selector: 'app-button',
  template: `
    <button [disabled]="isLoading">
      {{ isLoading ? 'Loading...' : 'Submit' }}
    </button>
  `
})
export class ButtonComponent {
  isLoading = false;
}
```

Khi `isLoading` là `true`, button sẽ bị disable. Khi thay đổi, button tự động enable/disable.

### Binding CSS Class

Một trong những cách dùng phổ biến nhất của property binding là thay đổi CSS class:

```html
<!-- Bind single class -->
<div [class.active]="isActive">Content</div>

<!-- Giải thích: Nếu isActive = true, class 'active' sẽ được thêm vào element -->
```

Điều này rất hữu ích cho:
- Highlight item hiện tại: `[class.selected]="item.id === selectedId"`
- Hiển thị trạng thái: `[class.error]="form.invalid"`
- Toggle menu: `[class.open]="isMenuOpen"`

### Binding CSS Style

Property binding cũng cho phép thay đổi inline styles:

```html
<div [style.color]="textColor">Colored text</div>
<div [style.font-size]="fontSize + 'px'">Dynamic text size</div>
```

### Truyền dữ liệu giữa Component (Component Interaction)

Property binding là cách để parent component gửi dữ liệu tới child component:

```typescript
@Component({
  selector: 'app-parent',
  template: `<app-child [message]="parentData"></app-child>`
})
export class ParentComponent {
  parentData = 'Data from parent';
}

@Component({
  selector: 'app-child',
  template: `<p>{{ message }}</p>`
})
export class ChildComponent {
  @Input() message: string = '';
}
```

Parent component sử dụng `[message]="parentData"` để gửi dữ liệu tới child component, child component nhận qua `@Input()`.

### Loại property phổ biến có thể bind

Angular cho phép bind vào hầu hết các property của HTML element:
- **value**: `[value]="inputValue"`
- **disabled**: `[disabled]="!canSubmit"`
- **href**: `[href]="url"`
- **src**: `[src]="imageUrl"`
- **title**: `[title]="tooltipText"`
- **placeholder**: `[placeholder]="hint"`
- **class**: `[class.className]="condition"`
- **style**: `[style.color]="dynamicColor"`

### Bảo mật và Type Safety

Một điểm mạnh của Angular property binding là nó có **type checking** tại compile time. Nếu bạn cố gắng bind vào một property không tồn tại hoặc type không phù hợp, Angular sẽ báo lỗi ngay lập tức.

---

## 3. Event Binding

### Định nghĩa

Event binding cho phép bạn lắng nghe các sự kiện xảy ra từ template (click, input, blur, submit, v.v.) và gọi một method trong component để xử lý sự kiện đó.

### Cơ chế hoạt động

Khi user tương tác với element (ví dụ click button):
1. Browser trigger DOM event
2. Angular detect sự kiện này
3. Angular gọi handler method trong component
4. Method có thể thay đổi component state
5. Change detection chạy, cập nhật template nếu cần

### Cú pháp

```html
(eventName)="handler($event)"
```

Dấu tròn `( )` báo cho Angular biết đây là event binding. `$event` là một đối tượng chứa thông tin chi tiết về sự kiện.

### Các loại sự kiện phổ biến

**Mouse Events:**
- `(click)`: Khi user click element
- `(dblclick)`: Khi user double-click
- `(mouseover)`: Khi mouse move vào element
- `(mouseout)`: Khi mouse move ra khỏi element
- `(mousedown)`: Khi user nhấn mouse button
- `(mouseup)`: Khi user thả mouse button

**Keyboard Events:**
- `(keydown)`: Khi user nhấn phím
- `(keyup)`: Khi user thả phím
- `(keyup.enter)`: Khi user nhấn phím Enter
- `(keyup.escape)`: Khi user nhấn phím Esc

**Form Events:**
- `(input)`: Khi nội dung input thay đổi
- `(change)`: Khi input blur (mất focus)
- `(blur)`: Khi element mất focus
- `(focus)`: Khi element nhận focus
- `(submit)`: Khi form được submit

### Ví dụ cơ bản

```typescript
@Component({
  selector: 'app-counter',
  template: `
    <p>Count: {{ count }}</p>
    <button (click)="increment()">Increment</button>
  `
})
export class CounterComponent {
  count = 0;
  
  increment() {
    this.count++;
  }
}
```

Mỗi lần user click button, method `increment()` được gọi, `count` tăng 1, và template tự động cập nhật.

### Truy cập thông tin sự kiện với $event

Trong nhiều trường hợp, bạn cần truy cập thông tin chi tiết về sự kiện. Object `$event` chứa:

```typescript
@Component({
  selector: 'app-search',
  template: `
    <input (input)="onSearch($event)" 
           placeholder="Search..." />
    <p>Search term: {{ searchTerm }}</p>
  `
})
export class SearchComponent {
  searchTerm = '';
  
  onSearch(event: Event) {
    const inputElement = event.target as HTMLInputElement;
    this.searchTerm = inputElement.value;
  }
}
```

Trong ví dụ này:
- `event` là DOM event object
- `event.target` là element đã trigger sự kiện (input element)
- `inputElement.value` là giá trị hiện tại của input

### Event Binding vs Two-Way Binding

Một câu hỏi hay gặp: tại sao không dùng event binding thay vì two-way binding?

**Dùng Event Binding:**
```html
<input (input)="onInput($event)" />
```
Ưu điểm: Kiểm soát chính xác những gì xảy ra khi có thay đổi

**Dùng Two-Way Binding:**
```html
<input [(ngModel)]="value" />
```
Ưu điểm: Code ngắn gọn, đơn giản

**Nên dùng cái nào?** Tùy vào tình huống. Với form đơn giản, two-way binding phù hợp hơn. Với form phức tạp hoặc khi cần kiểm soát chi tiết, event binding tốt hơn.

### Custom Event Binding

Không chỉ các event chuẩn, Angular component có thể phát ra (emit) custom event:

```typescript
@Component({
  selector: 'app-child',
  template: `<button (click)="notifyParent()">Notify</button>`
})
export class ChildComponent {
  @Output() myEvent = new EventEmitter<string>();
  
  notifyParent() {
    this.myEvent.emit('Data from child');
  }
}

@Component({
  selector: 'app-parent',
  template: `<app-child (myEvent)="handleEvent($event)"></app-child>`
})
export class ParentComponent {
  handleEvent(data: string) {
    console.log(data);
  }
}
```

Đây là cách child component giao tiếp ngược lại với parent.

---

## 4. Two-Way Binding

### Định nghĩa

Two-way binding là sự kết hợp của property binding và event binding, cho phép dữ liệu tự động đồng bộ hóa giữa component và template theo cả hai chiều. Khi component data thay đổi, template cập nhật. Khi user thay đổi template (ví dụ input text), component data cũng cập nhật.

### Cơ chế hoạt động

Two-way binding hoạt động dựa trên một quy ước:

```html
<!-- Two-way binding (cách viết ngắn) -->
<input [(ngModel)]="name" />

<!-- Tương đương với (cách viết dài) -->
<input [ngModel]="name" (ngModelChange)="name = $event" />
```

- `[ngModel]="name"` - Property binding, bind giá trị component vào input
- `(ngModelChange)="name = $event"` - Event binding, khi input thay đổi, cập nhật component

### Cú pháp

```html
[(ngModel)]="propertyName"
```

Dấu `[( )]` gọi là "banana-in-a-box" - cách viết tắt cho sự kết hợp của property binding `[ ]` và event binding `( )`.

### Prerequisite: FormsModule

Để sử dụng `ngModel`, bạn cần import `FormsModule`. Cách này khác nhau tùy vào phiên bản Angular:

**Angular 14+ (Standalone Component):**
```typescript
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-root',
  template: `<input [(ngModel)]="name" />`,
  imports: [FormsModule]
})
export class AppComponent {
  name = '';
}
```

**Angular cũ (NgModule):**
```typescript
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';

@NgModule({
  imports: [FormsModule]
})
export class AppModule { }
```

### Khi nào sử dụng Two-Way Binding

**Nên dùng:**
- Form đơn giản với một vài field
- Search box, filter
- Quick CRUD operation
- Khi không cần validation phức tạp

**Không nên dùng:**
- Form phức tạp với nhiều field, validation -> Dùng **Reactive Forms**
- Khi cần kiểm soát chính xác flow của data
- Khi cần validate từng field riêng lẻ

### Ví dụ áp dụng

```typescript
@Component({
  selector: 'app-user-edit',
  template: `
    <div>
      <h2>Edit User</h2>
      <input [(ngModel)]="user.firstName" placeholder="First name" />
      <input [(ngModel)]="user.lastName" placeholder="Last name" />
      <input [(ngModel)]="user.email" placeholder="Email" />
      
      <div class="preview">
        <p>Name: {{ user.firstName }} {{ user.lastName }}</p>
        <p>Email: {{ user.email }}</p>
      </div>
    </div>
  `,
  imports: [FormsModule]
})
export class UserEditComponent {
  user = {
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com'
  };
}
```

Mỗi khi user nhập vào input, `user` object tự động cập nhật. Mỗi khi `user` thay đổi ở component, input tự động cập nhật.

### Ưu điểm và Nhược điểm

**Ưu điểm:**
- Đơn giản, code ngắn gọn
- Dễ hiểu, dễ học
- Phù hợp với form đơn giản

**Nhược điểm:**
- Không có validation
- Khó kiểm soát khi có logic phức tạp
- Performance có thể bị ảnh hưởng với form lớn

### Two-Way Binding vs Reactive Forms

Tại sao có cả hai? Vì:
- **Two-way binding**: Phù hợp cho form đơn giản, quick prototype
- **Reactive Forms**: Phù hợp cho form phức tạp, cần validation, cần kiểm soát chính xác

Angular cung cấp cả hai để bạn có sự lựa chọn tùy vào nhu cầu.

---

## Thay đổi từ Angular cũ đến Angular 19

### 1. Standalone Components vs NgModule

**Angular cũ (NgModule way):**
```typescript
// app.module.ts
@NgModule({
  declarations: [AppComponent],
  imports: [BrowserModule, FormsModule]
})
export class AppModule { }

// app.component.ts
@Component({
  selector: 'app-root',
  template: `...`
})
export class AppComponent { }
```

**Angular 19 (Standalone way):**
```typescript
// app.component.ts - Standalone component
@Component({
  selector: 'app-root',
  template: `...`,
  imports: [FormsModule]  // Import trực tiếp
})
export class AppComponent { }
```

Standalone components đơn giản hóa cấu trúc project, không cần quản lý NgModule.

### 2. Signals thay vì Observable

**Angular cũ:**
```typescript
export class Component {
  count$ = of(0);
  
  increment() {
    // Không thể thay đổi trực tiếp, phải sử dụng Subject
  }
}
```

**Angular 19 (với Signals):**
```typescript
import { signal } from '@angular/core';

export class Component {
  count = signal(0);
  
  increment() {
    this.count.set(this.count() + 1);
  }
}
```

Signals cung cấp cách đơn giản hơn để quản lý reactive state, nhất là cho các giá trị đơn giản không cần đầu vào stream phức tạp.

### 3. Typing của $event

**Angular cũ:**
```html
<input (input)="onChange($event)" />

<!-- Trong component, event là any -->
onChange(event: any) {
  const value = event.target.value;
}
```

**Angular 19 (Type-safe):**
```html
<input (input)="onChange($event)" />

<!-- Type-safe, event là Event -->
onChange(event: Event) {
  const value = (event.target as HTMLInputElement).value;
}
```

Angular 19 cung cấp type information chính xác cho events, giúp phát hiện lỗi sớm.

---

## Kỹ năng cốt lõi

### 1. Chọn loại binding phù hợp

Đây là kỹ năng quan trọng nhất. Bảng dưới so sánh 4 loại binding:

| Situation | Loại Binding | Lý do |
|-----------|-------------|------|
| Hiển thị dữ liệu text | Interpolation | Đơn giản, rõ ràng |
| Bind property HTML (value, disabled, src) | Property Binding | Cần thay đổi động |
| Bind CSS class dựa trên condition | Property Binding (`[class.xxx]`) | Đơn giản, hiệu quả |
| Xử lý click, input event | Event Binding | Template→Component |
| Form input cần đồng bộ 2 chiều | Two-Way Binding | Đơn giản, form nhỏ |
| Form lớn, validation phức tạp | Reactive Forms | Kiểm soát chính xác |

### 2. Hiểu Change Detection

Data binding hoạt động dựa trên change detection của Angular. Nếu dữ liệu thay đổi nhưng template không cập nhật, có thể change detection không detect được thay đổi.

**Điều này xảy ra khi:**
- Data thay đổi ngoài Angular zone (ví dụ setTimeout từ external library)
- Object được mutate nhưng reference không đổi
- Array được thay đổi qua index thay vì push/pop

### 3. Performance optimization

- Tránh gọi method trong interpolation (tính toán lại mỗi change detection)
- Sử dụng `OnPush` change detection strategy nếu component không thường xuyên thay đổi
- Với dữ liệu lớn, cân nhắc sử dụng `trackBy` trong `*ngFor`

---

## Tóm tắt

Data binding là nền tảng của Angular development. Hiểu rõ 4 loại binding, khi nào sử dụng từng loại, và cách chúng hoạt động sẽ giúp bạn viết code hiệu quả, dễ maintain, và avoid common pitfalls.

**Key takeaways:**
- Interpolation: Display data
- Property Binding: Bind DOM properties
- Event Binding: Handle user interaction
- Two-Way Binding: Sync data both ways
- Chọn loại binding phù hợp cho mỗi situation
- Hiểu change detection mechanism

---

## Tham khảo

- [Angular Templates Guide - Binding](https://angular.dev/guide/templates/binding)
- [Angular Event Binding](https://angular.dev/guide/templates/events)
- [Angular Two-Way Binding](https://angular.dev/guide/templates/two-way-binding)
- [Angular Style Guide - Best Practices](https://angular.dev/style-guide)
