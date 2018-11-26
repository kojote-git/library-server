SELECT
  book.id, book.title, SUM(downloads.dCount) AS downloads
FROM Book book
LEFT JOIN (
  SELECT
    bookInstance.bookId AS bookId,
    downloads.dateDownloaded AS dateDownloaded,
    SUM(downloads.dCount) AS dCount
  FROM BookInstance bookInstance
  LEFT JOIN (
    SELECT
      bookInstanceId,
      DATE(dateDownloaded) AS dateDownloaded,
      COUNT(*) AS dCount
    FROM Download
    GROUP BY bookInstanceId, DATE(dateDownloaded)
  ) AS downloads
  ON bookInstance.id = downloads.bookInstanceId
  GROUP BY bookInstance.bookId, dateDownloaded
) AS downloads
ON downloads.bookId = book.id
WHERE downloads.dateDownloaded BETWEEN ? AND ?
GROUP BY book.id, book.title
ORDER BY downloads DESC;