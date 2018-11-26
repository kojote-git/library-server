SELECT
  author.id, author.firstName, author.middleName, author.lastName,
  SUM(workDownloads.dCount) AS downloads
FROM Author author
LEFT JOIN WorkAuthor wa ON author.id = wa.authorId
LEFT JOIN (
  SELECT
    book.workId, SUM(downloads.dCount) AS dCount
  FROM Book book
  LEFT JOIN (
    SELECT
      bookInstance.bookId AS bookId,
      SUM(downloads.dCount) AS dCount
    FROM BookInstance bookInstance
    LEFT JOIN (
      SELECT
        bookInstanceId,
        COUNT(*) AS dCount
      FROM Download
      GROUP BY bookInstanceId
    ) AS downloads
    ON bookInstance.id = downloads.bookInstanceId
    GROUP BY bookInstance.bookId
  ) AS downloads
  ON downloads.bookId = book.id
  GROUP BY book.workId
) AS workDownloads
ON wa.workId = workDownloads.workId
GROUP BY author.id, author.firstName, author.middleName, author.lastName
ORDER BY downloads DESC;