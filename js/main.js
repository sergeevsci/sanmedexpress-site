const form = document.querySelector('#request');
const formMessage = document.querySelector('#formMessage');
const scrollToFormButton = document.querySelector('[data-scroll-to-form]');

if (scrollToFormButton && form) {
  scrollToFormButton.addEventListener('click', () => {
    setTimeout(() => form.scrollIntoView({ behavior: 'smooth', block: 'start' }), 220);
  });
}

if (form) {
  form.addEventListener('submit', (event) => {
    event.preventDefault();

    const name = form.elements.name;
    const phone = form.elements.phone;
    const consent = form.elements.consent;
    const fields = [name, phone];
    let isValid = true;

    fields.forEach((field) => {
      const empty = !field.value.trim();
      field.classList.toggle('field-error', empty);
      if (empty) isValid = false;
    });

    consent.classList.toggle('field-error', !consent.checked);
    if (!consent.checked) isValid = false;

    if (!isValid) {
      formMessage.textContent = 'Заполните имя, телефон и подтвердите согласие на обработку данных.';
      formMessage.className = 'form-message error';
      return;
    }

    const preparedRequest = {
      name: name.value.trim(),
      phone: phone.value.trim(),
      comment: form.elements.comment.value.trim(),
      createdAt: new Date().toISOString(),
    };

    console.info('Prepared request:', preparedRequest);
    form.reset();
    formMessage.textContent = 'Спасибо! Заявка подготовлена. Для срочного заказа позвоните по телефону +7 905 769-03-03.';
    formMessage.className = 'form-message success';
  });

  form.addEventListener('input', (event) => {
    if (event.target.matches('input, textarea')) {
      event.target.classList.remove('field-error');
    }
  });

  form.addEventListener('change', (event) => {
    if (event.target.matches('input[type="checkbox"]')) {
      event.target.classList.remove('field-error');
    }
  });
}
