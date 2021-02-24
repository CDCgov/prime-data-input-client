import React from "react";
import classnames from "classnames";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faAngleLeft, faAngleRight } from "@fortawesome/free-solid-svg-icons";
import "./Pagination.scss";

interface Props {
  baseRoute: string;
  totalEntries: number;
  entriesPerPage: number;
  currentPage: number;
  pageGroupSize?: number;
  className?: string;
}

// Rules:
// * Always show first/last page (may be the same page!)
// * Show current page and N pages on either side
// * Use elipsis if gaps between group and first/last
//
// Cases (_n_ represents currentPage):
//  << _1_ >>
//  << 1 _2_ 3 >>
//  << _1_ 2 3 4 5 ... 71  >>
//  << 1 2 _3_ 4 5 ... 71  >>
//  << 1 ... 14 15 _16_ 17 18 ... 71 >>
//  << 1 ... 67 68 _69_ 70 71 >>
//  << 1 ... 67 68 69 70 _71_ >>
//  << 1 2 3 _4_ 5 6 ... 71 >>

// Always make this odd, current page is in the middle
const defaultGroupSize = 5;

const Pagination = ({
  baseRoute,
  currentPage: rawCurrentPage,
  entriesPerPage,
  totalEntries,
  pageGroupSize = defaultGroupSize,
  className,
}: Props) => {
  const groupGutter = Math.floor((pageGroupSize || defaultGroupSize) / 2);
  const totalPages = Math.ceil(totalEntries / entriesPerPage);
  const currentPage = Math.min(Math.max(+rawCurrentPage || 0, 1), totalPages);
  const minGroupPage = Math.max(1, currentPage - groupGutter);
  const maxGroupPage = Math.min(currentPage + groupGutter, totalPages);
  const pageList = [];

  // Build list of pages, with 0 representing the ellipsis
  if (minGroupPage !== 1) {
    pageList.push(1);
  }
  if (minGroupPage > groupGutter) {
    pageList.push(0);
  }
  for (let pn = minGroupPage; pn <= maxGroupPage; pn++) {
    pageList.push(pn);
  }
  if (maxGroupPage < totalPages - groupGutter) {
    pageList.push(0);
  }
  if (maxGroupPage !== totalPages) {
    pageList.push(totalPages);
  }

  return (
    <nav
      className={classnames("usa-pagination", className)}
      role="navigation"
      aria-label="Pagination"
    >
      <ol>
        {currentPage > 1 && (
          <li>
            <a
              href={`${baseRoute}/${currentPage - 1}`}
              aria-label="Previous Page"
            >
              <FontAwesomeIcon icon={faAngleLeft} /> Prev
            </a>
          </li>
        )}
        {pageList.map((pn) =>
          pn ? (
            <li>
              <a
                href={`${baseRoute}/${pn}`}
                aria-label={`Page ${pn}`}
                className={pn === currentPage ? "is-active" : ""}
              >
                <span>{pn}</span>
              </a>
            </li>
          ) : (
            <li aria-hidden="true">…</li>
          )
        )}
        {currentPage < totalPages && (
          <li>
            <a href={`${baseRoute}/${currentPage + 1}`} aria-label="Next Page">
              Next <FontAwesomeIcon icon={faAngleRight} />
            </a>
          </li>
        )}
      </ol>
    </nav>
  );
};

export default Pagination;
