async function apiFetch(url, options = {}) {
  let response = await fetch(url, {
    ...options,
    credentials: "include",
  });

  // Token hết hạn
  if (response.status === 401) {
    const refreshResponse = await fetch("/api/auth/refresh", {
      method: "POST",
      credentials: "include",
    });

    if (refreshResponse.ok) {
      // gọi lại request ban đầu
      response = await fetch(url, {
        ...options,
        credentials: "include",
      });
    } else {
      // refresh token cũng hết hạn
      window.location.href = "/admin/login";
    }
  }

  return response;
}
