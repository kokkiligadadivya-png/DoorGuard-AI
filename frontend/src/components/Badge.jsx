import { levelClass, levelIcon } from "../utils";

export default function Badge({ level }) {
  return (
    <span className={`badge ${levelClass(level)}`}>
      {levelIcon[level]} {level}
    </span>
  );
}