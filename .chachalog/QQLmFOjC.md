---
legacy-default-components: patch
---

Fixed the mode dispatcher component so that characters such as `<` or `&` in its mode display as text in edit mode.

A mode that is not a render mode name loads the content as if no mode were set.
